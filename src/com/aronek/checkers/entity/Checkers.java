package com.aronek.checkers.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.aronek.checkers.CheckersSessionManager;
import com.aronek.checkers.Constants;
import com.aronek.checkers.Message;
import com.aronek.checkers.entity.Game.Status;
import com.aronek.checkers.model.Action;
import com.aronek.checkers.model.CheckerException;
import com.aronek.checkers.model.GameCleaner;
import com.aronek.checkers.model.RandomString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Checkers {
	
	public static final long MAX_NUMBER_OF_GAMES = 1000000L;
	public static final long INACTIVE_TIME = 10 * 60 * 1000L;
	public static Map<Long, Game> games = new HashMap<Long, Game>();
	public static Map<String, Player> players = new HashMap<String, Player>();
	private static RandomString randomString = new RandomString(32);
	
	// the data includes: the name of the player
	public static void createGame(String data, Session session) throws Exception {
		JsonObject jsonObject = parseToJsonObject(data);
		String playerName = jsonObject.get("name").getAsString();
		int boardSize = jsonObject.get("boardSize").getAsInt();
		JsonObject rulesObject = jsonObject.get("rules").getAsJsonObject();
		Rules rules = createRules(rulesObject);
		long gameCode = getGameId();
		Player creator = createPlayer(playerName, session, Player.CREATOR_ID);
		Game game = new Game(creator, gameCode, boardSize);
		games.put(gameCode, game);
		creator.setGame(game); 
		Map<String, Object> feedback = getCreateGameFeedback(gameCode);
		sendMessage(session, Action.CREATE.getNumber(), feedback);
	}
	
	private static Rules createRules(JsonObject rulesObject) {
		Rules rules = new Rules();
		rules.canPieceCaptureBackwards = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		rules.canKingMoveMoreThanOneStep = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		rules.shouldPieceContinueCapturingAfterFarthestRow = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		rules.shouldCaptureWhenPossible = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		rules.shouldCaptureMaxPossible = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		rules.shouldDiscardCapturedPieceMomentarily = rulesObject.get("canKingMoveMoreThanOneStep").getAsBoolean();
		return rules;
	}
	
	private static Player getAsynchPlayer(String sessionId)  {
		Player player = players.get(sessionId);
		if (player != null) {
			player.updateLastAccessed();
		}
		return player;
	}

	private static void sendMessage(Session session, int code, Map<String, Object> feedback) throws IOException, EncodeException {
		CheckersSessionManager.publish(getMessage(code, feedback), session);
	}

	private static Player createPlayer(String name, Session session, int playerId) {
		String token = getSessionToken(session);
		Player player = new Player(session, name, playerId);
		player.setToken(token); 
		players.put(token, player);
		return player;
	}

	private static Map<String, Object> getCreateGameFeedback(long gameCode) {
		Map<String, Object> feedback = new HashMap<String, Object>();
		feedback.put("gameCode", gameCode);
		feedback.put("playerId", Player.CREATOR_ID);
		return feedback;
	}
	
	public static void joinGame(String data, Session session) throws Exception { 
		JsonObject jsonObject = parseToJsonObject(data);
		Game game = getGame(jsonObject);
		synchronized (game) {
			// prevent more than one player to join the game 
			createJoiner(session, jsonObject, game); 
		}
		game.initBoard();
		game.setStarted();
		game.setPlayerInTurn(game.getJoiner()); 
		updateNewGame(session, game);
	}

	private static void updateNewGame(Session session, Game game) throws IOException, EncodeException {
		Map<String, Object> creatorFeedback = getInfoFeedback(String.format("%s has joined the game", game.getJoiner().getName()));
		sendMessage(game.getCreator().getSession(), Action.ACTION_OTHER_JOINED.getNumber(), creatorFeedback);
		Map<String, Object> joinerFeedback = new HashMap<String, Object>();
		joinerFeedback.put("playerId", Player.JOINER_ID);
		sendMessage(session, Action.JOIN.getNumber(), joinerFeedback);
		updateGameStatus(game);
	}

	private static void updateGameStatus(Game game) throws IOException, EncodeException {
		Map<String, Object> board = getPlayFeedback(game);
		updateStatus(game.getCreator(), board);
		updateStatus(game.getJoiner(), board);
	}
	
	public static void updateStatus(Player player, Map<String, Object> board) throws IOException, EncodeException {
		sendMessage(player.getSession(), Action.STATE.getNumber(), board); 
	}

	public static Map<String, Object> getPlayFeedback(Game game) {
		Map<String, Object> board = new HashMap<String, Object>();
		board.put("turn", game.getPlayerInTurn().getId());
		board.put("checkers", game.getCheckers());
		board.put("creator", game.getCreatorName());
		board.put("joiner", game.getJoinerName());
		board.put("status", game.getStatus());
		board.put("boardSize", game.getBoardSize());
		board.put("vchatUuid", game.getVideoChatUuid());
		return board;
	}

	private static Player createJoiner(Session session, JsonObject jsonObject, Game game) throws Exception { 
		if (game.isNew()) {
			String playerName = jsonObject.get("name").getAsString();
			Player joiner = createPlayer(playerName, session, Player.JOINER_ID);
			game.setJoiner(joiner);
			game.setStatus(Status.READY);
			return joiner;
		}
		throw new CheckerException("not allowed to join the game.");
	}

	private static JsonObject parseToJsonObject(String data) {
		return JsonParser.parseString(data).getAsJsonObject();
	}
	
	private static JsonArray parseToJsonArray(String data) {
		return JsonParser.parseString(data).getAsJsonArray();
	}

	private static Game getGame(JsonObject jsonObject) {
		long gameCode = jsonObject.get("code").getAsLong();
		Game game = games.get(gameCode);
		Objects.requireNonNull(game);
		return game;
	}

	private static Map<String, Object> getInfoFeedback(String message) {
		Map<String, Object> creatorFeedback = new HashMap<String, Object>();
		creatorFeedback.put("info", message);
		return creatorFeedback;
	}
	
	private static Message getMessage(int code, Object data) {
		Gson gson = new GsonBuilder()
				  .excludeFieldsWithoutExposeAnnotation()
				  .create();
		return new Message(code, gson.toJson(data));
	}

	// synchronized to ensure the id is not generated while the method is running
	public synchronized static Player getPlayer(String token) throws Exception {
		Player player = getAsynchPlayer(token);
		return player;
	}

	public static String getSessionToken(Session session) {
		return (String) session.getUserProperties().get(Constants.TOKEN);
	}

	public synchronized static String generatePlayerToken() throws Exception {
		while (true) {
			String token = randomString.nextString();
			if (!players.containsKey(token)) {
				return token;
			}
		}
	}
	
	public synchronized static long getGameId() throws Exception { 
        for (long id = 0; id < MAX_NUMBER_OF_GAMES; id++) {
        	if (!games.containsKey(id)) {
        		return id;
        	}
        }
        throw new CheckerException("max games reached, try again later");
    }

	public static void leaveGame(Session session, String token) throws IOException, EncodeException, CheckerException {  
		if (token == null) {
			token = getSessionToken(session);
		}
		Player player = getAsynchPlayer(token);
		throwExceptionIfNoPlayer(player);
		Game game = player.getGame();
		synchronized (game) {
			leaveGame(game, player, token);
		}
	}
	
	private static void throwExceptionIfNoPlayer(Player player) throws CheckerException { 
		if (player == null) {
			throw new CheckerException("No player");
		}
	}

	private static void leaveGame(Game game, Player player, String token) throws IOException, EncodeException {
		game.setStatus(Game.Status.TERMINATED);
		Player other = game.getOtherPlayer(player); 
		players.remove(token);
		sendMessage(player.getSession(), Action.CLOSE.getNumber(), getInfoFeedback("you left the game"));
		if (other != null) {
			sendMessage(other.getSession(), Action.OTHER_CLOSE.getNumber(), getInfoFeedback(player.getName() + " left the game"));
		} else {
			games.remove(game.getId());
		}
		game.removePlayer(player);
		player.getSession().close();
		System.out.println("play removed completely");
	}

	public static void restartGame(Session session) throws Exception {
		String token = getSessionToken(session);
		Player creator = getAsynchPlayer(token);
		throwExceptionIfNoPlayer(creator);
		Game game = creator.getGame();
		synchronized (game) {
			restartGame(game, creator);
		}
	}

	private static void restartGame(Game game, Player creator) throws Exception { 
		game.throwExceptionIfNotStartable();
		game.throwExceptionIfNotCreator(creator);
		game.initBoard();
		game.setStarted();
		Map<String, Object> infoFeedback = getInfoFeedback("game restarted");
		sendMessage(creator.getSession(), Action.INFO.getNumber(), infoFeedback);
		sendMessage(game.getJoiner().getSession(), Action.INFO.getNumber(), infoFeedback);
		updateGameStatus(game);
	}

	public static void play(String data, Session session) throws Exception { 
		String token = getSessionToken(session);
		Player player = getAsynchPlayer(token);
		Game game = player.getGame();
		synchronized(game) {
			play(game, player, data);
		}
	}

	private static void play(Game game, Player player, String data) throws Exception {  
		JsonArray plays = parseToJsonArray(data);
		game.updatePlay(player, plays);
		Player otherPlayer = player.getOtherPlayer();
		sendPlayUpdate(otherPlayer, plays);
		updateGameOver(game);
	}

	private static void updateGameOver(Game game) throws IOException, EncodeException { 
		if (game.isGameOver()) {
			Player winner = game.getWinner();
			Map<String, Object> winnerFeedback = new HashMap<String, Object>();
			winnerFeedback.put("winnerId", winner.getId());
			sendMessage(game.getCreator().getSession(), Action.OVER.getNumber(), winnerFeedback);
			sendMessage(game.getJoiner().getSession(), Action.OVER.getNumber(), winnerFeedback);
		}
	}

	private static void sendPlayUpdate(Player player, JsonArray plays) throws IOException, EncodeException { 
		Map<String, Object> playFeedback = new HashMap<String, Object>();
		playFeedback.put("plays", plays.toString());
		sendMessage(player.getSession(), Action.PLAY.getNumber(), playFeedback); 
	}

	public static void getGameState(Session session) throws IOException, EncodeException { 
		String token = getSessionToken(session);
		Player player = getAsynchPlayer(token);
		if (player != null) {
			Game game = player.getGame();
			synchronized (game) {
				if (!game.isNew()) {
					Map<String, Object> board = Checkers.getPlayFeedback(player.getGame());
					updateStatus(player, board);
				}
			}
		}
	}

	public static void chat(String text, Session session) throws IOException, EncodeException { 
		String token = getSessionToken(session);
		Player player = getAsynchPlayer(token);
		Game game = player.getGame();
		game.addChat(player, text);
		Player receiver = player.getOtherPlayer();
		synchronized(game) {
			chat(player, receiver, text);
		}
	}
	
	private static void chat(Player player, Player receiver, String text) throws IOException, EncodeException { 
		if (receiver != null && receiver.getSession().isOpen()) {
			Map<String, Object> feedback = new HashMap<String, Object>();
			feedback.put("chat", text);
			sendMessage(receiver.getSession(), Action.CHAT.getNumber(), feedback);
		} else {
			Map<String, Object> feedback = new HashMap<String, Object>();
			feedback.put("chat", "***Your opponent is not available***");
			sendMessage(player.getSession(), Action.CHAT.getNumber(), feedback);
		}
	}


	public static void cleanUpGame(Session session) throws InterruptedException {
		String token = Checkers.getSessionToken(session);
		Player player = getAsynchPlayer(token);
		if (player == null) {
			return;
		}
		// creating an instance of timer class 
        Timer timer = new Timer();  
        // creating an instance of task to be scheduled 
        TimerTask task = new GameCleaner(player, token); 
        // scheduling the timer instance 
        timer.schedule(task, 5 * 1000);
        synchronized(player) {
        	player.wait();
        }
        task.cancel();
    	timer.cancel();
	}

}
