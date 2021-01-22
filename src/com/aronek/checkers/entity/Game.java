package com.aronek.checkers.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.aronek.checkers.entity.Piece.Type;
import com.aronek.checkers.model.CheckerException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Game {
	
	private long id;
	private int boardSize;
	private int joinerPiecesBeginRow;
	private int numberOfRowsPiecesOccupy;
	private Player creator;
	private Player joiner;
	private Player playerInTurn;
	private Player winner;
	private Checker[][] checkers;
	private Status status;
	private List<Chat> chats;
	private List<String> fromCreatorChats = new ArrayList<String>();
	private List<String> fromJoinerChats = new ArrayList<String>();
	private String videoChatUuid;
	private Rules rules;
	
	public Game(Player creator, long id, int boardSize, Rules rules) {
		this.id = id;
		this.boardSize = boardSize;
		this.creator = creator;
		this.setStatus(Status.NEW);
		this.setJoinerPiecesBeginRow();
		this.createVideoChatUuid();
		this.rules = rules;
	}
	
	private final void setJoinerPiecesBeginRow() {
		if (boardSize == 8) {
			joinerPiecesBeginRow = 5;
			numberOfRowsPiecesOccupy = 3;
		} else {
			joinerPiecesBeginRow = 6;
			numberOfRowsPiecesOccupy = 4;
		}
	}

	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}
	
	public int getBoardSize() {
		return boardSize;
	}


	public Player getCreator() {
		return creator;
	}


	public void setCreator(Player creator) {
		this.creator = creator;
	}


	public Player getJoiner() {
		return joiner;
	}


	public void setJoiner(Player joiner) {
		this.joiner = joiner;
		joiner.setGame(this); 
	}


	public Player getPlayerInTurn() {
		return playerInTurn;
	}


	public void setPlayerInTurn(Player playerInTurn) {
		this.playerInTurn = playerInTurn;
	}


	public Checker[][] getCheckers() {
		return checkers;
	}


	public void setCheckers(Checker[][] checkers) {
		this.checkers = checkers;
	}

	public boolean isGameOver() {
		return status == Status.OVER;
	}
	
	public Player getWinner() {
		return winner;
	}

	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public List<Chat> getChats() {
		return chats;
	}


	public void setChats(List<Chat> chats) {
		this.chats = chats;
	}
	
	public void setRules(Rules rules) {
		this.rules = rules;
	}
	
	public Rules getRules() {
		return rules;
	}
	
	public int getLastRowIndex() {
		return boardSize - 1;
	}
	
	public Player getOtherPlayer(Player player) {
		if (player == creator) {
			return joiner;
		}
		return creator;
	}
	
	public enum Status {
		NEW, READY, STARTED, OVER, TERMINATED 
	}
	
	public void setStarted() {
		status = Status.STARTED;
	}

	public void initBoard() {
		initCheckers();
		// creator pieces begin at row 0 of the board
		initPieces(0, creator);
		initPieces(joinerPiecesBeginRow, joiner);
	}

	private void initPieces(int startRow, Player owner) {
		for (int row = startRow; row < startRow + numberOfRowsPiecesOccupy; row++) {
			for (int col = 0; col < boardSize; col++) {
				if ((row + col) % 2 == 1) {
					Checker checker = checkers[row][col];
					Piece piece = new Piece(owner);
					piece.setType(Type.NORMAL.getNumber());
					piece.setChecker(checker);
					checker.setPiece(piece); 
				}
			}
		}
	}

	private void initCheckers() {
		checkers = new Checker[boardSize][boardSize];
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				checkers[i][j] = new Checker(i, j);
			}
		}
	}


	public boolean isCreator(Player player) {
		return player == creator;
	}


	public boolean isNew() {
		return status == Status.NEW;
	}

	public boolean isReady() { 
		return status == Status.READY;
	}

	public void throwExceptionIfNotStartable() throws Exception { 
		if (joiner == null || status != Status.OVER) {
			throw new CheckerException("not allowed");
		}
	}


	public synchronized void updatePlay(Player player, JsonArray plays) throws Exception {
		throwExceptionIfNotPlayable();
		throwExceptionIfNotInTurn(player);
		for (int i = 0; i < plays.size(); i++) {
			updatePlay(plays.get(i).getAsJsonObject());
		}
		processPlayCompleted(plays);
		updatePlayerInTurn();
		processGameCompleted();
	}
	
	private void throwExceptionIfNotPlayable() throws Exception { 
		if (status != Status.STARTED) {
			throw new CheckerException("game not started");
		}
	}


	private void updatePlayerInTurn() {
		if (playerInTurn == creator) {
			playerInTurn = joiner;
		} else {
			playerInTurn = creator;
		}
	}
	
	private void processGameCompleted() {
		boolean creatorHasPlayablePieces = hasPlayablePieces(creator);
		boolean joinerHasPlayablePieces = hasPlayablePieces(joiner);
		if (!creatorHasPlayablePieces) {
			status = Status.OVER;
			winner = joiner;
		}
		if (!joinerHasPlayablePieces) {
			status = Status.OVER;
			winner = creator;
		}
	}

	private boolean hasPlayablePieces(Player player) {
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				Piece piece = checkers[i][j].getPiece();
				if (piece != null && piece.getOwner() == player) {
					if (piece.isPiecePlayable(this)) {
						return true;
					}
				}
				
			}
		}
		return false;
	}

	private void processPlayCompleted(JsonArray plays) {
		JsonObject lastPlay = plays.get(plays.size() - 1).getAsJsonObject();
		JsonObject toPosition = lastPlay.get("to").getAsJsonObject();
		Checker checker = getCheckerFromJsonPosition(toPosition);
		Piece piece = checker.getPiece();
		if (piece.isAtLastRow(getLastRowIndex())) {
			piece.setType(Piece.Type.KING.getNumber());
		}
	}


	private void updatePlay(JsonObject play) {
		JsonObject fromPosition = play.get("from").getAsJsonObject();
		JsonObject toPosition = play.get("to").getAsJsonObject();
		JsonElement capturedPiecePosition = play.get("captured");
		movePiece(fromPosition, toPosition);
		updateCaptured(capturedPiecePosition);
	}


	private void movePiece(JsonObject fromPosition, JsonObject toPosition) {
		Checker fromChecker = getCheckerFromJsonPosition(fromPosition);
		Checker toChecker = getCheckerFromJsonPosition(toPosition);
		toChecker.setPiece(fromChecker.getPiece());
		fromChecker.setPiece(null);
		toChecker.getPiece().setChecker(toChecker);
	}
	
	private Checker getCheckerFromJsonPosition(JsonObject position) {
		int row = position.get("row").getAsInt();
		int col = position.get("col").getAsInt();
		return checkers[row][col];
	}


	private void updateCaptured(JsonElement capturedPiecePosition) {
		if (!capturedPiecePosition.isJsonNull()) {
			JsonObject position = capturedPiecePosition.getAsJsonObject();
			Checker checker = getCheckerFromJsonPosition(position);
			checker.setPiece(null); 
		}
	}


	private void throwExceptionIfNotInTurn(Player player) throws Exception {
		if (player != playerInTurn) {
			throw new CheckerException("Player not in turn");
		}
	}


	public Checker getChecker(int row, int col) {
		if (isWithinBounds(row, col)) {
			return checkers[row][col];
		}
		return null;
	}


	private boolean isWithinBounds(int row, int col) {
		return indexIsWithinBounds(row) && indexIsWithinBounds(col);
	}


	private boolean indexIsWithinBounds(int index) {
		return index >= 0 && index <= getLastRowIndex();
	}


	public void throwExceptionIfNotCreator(Player player) throws Exception { 
		if (player != creator) {
			throw new CheckerException("You are not allowed");
		}
	}


	public String getCreatorName() {
		return creator != null ? creator.getName() : "creator";
	}
	
	public String getJoinerName() {
		return joiner != null ? joiner.getName() : "joiner";
	}


	public void removePlayer(Player player) {
		if (player == creator) {
			creator = null;
		} else {
			joiner = null;
		}
	}
	
	public void addChat(Player player, String text) {
		if (player.isCreator()) {
			fromCreatorChats.add(text);
		} else {
			fromJoinerChats.add(text);
		}
	}
	
	private void createVideoChatUuid() {
		UUID uuid = UUID.randomUUID();
		videoChatUuid = uuid.toString();
	}
	
	public String getVideoChatUuid() {
		return videoChatUuid;
	}
	
}
