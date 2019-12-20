package com.aronek.checkers.entity;

import java.util.List;
import com.aronek.checkers.entity.Piece.Type;

public class Game {
	
	private long id;
	private Player creator;
	private Player joiner;
	private Player playerInTurn;
	private Checker[][] checkers;
	private Status status;
	private List<Chat> chats;
	
	
	public Game(Player creator, long id) {
		this.id = id;
		this.creator = creator;
		this.setStatus(Status.NEW);
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
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
	
	public Player getOtherPlayer(Player player) {
		if (player == creator) {
			return joiner;
		}
		return creator;
	}
	
	public enum Status {
		NEW, READY, STARTED, OVER
	}

	public void initBoard() {
		status = Status.READY;
		initCheckers();
		// creator pieces begin at row 0 of the board
		initPieces(0, creator);
		// joiner pieces begin at row 5
		initPieces(5, joiner);
	}

	private void initPieces(int startRow, Player owner) {
		for (int row = startRow; row < startRow + 3; row++) {
			for (int col = 0; col < 8; col++) {
				if ((row + col) % 2 == 0) {
					Checker checker = checkers[row][col];
					Piece piece = new Piece(owner);
					piece.setType(Type.NORMAL);
					piece.setChecker(checker);
					checker.setPiece(piece); 
				}
			}
		}
	}

	private void initCheckers() {
		checkers = new Checker[8][8];
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				checkers[i][j] = new Checker(i, j);
			}
		}
	}
	
}
