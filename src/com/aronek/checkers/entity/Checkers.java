package com.aronek.checkers.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.aronek.checkers.CheckersSessionManager;
import com.aronek.checkers.Constants;
import com.aronek.checkers.Message;
import com.aronek.checkers.entity.Game.Status;
import com.aronek.checkers.model.Action;
import com.aronek.checkers.model.RandomString;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Checkers {
	
	public static final long MAX_NUMBER_OF_GAMES = 1000000L;
	public static Map<Long, Game> games = new HashMap<Long, Game>();
	private static Map<String, Player> players = new HashMap<String, Player>();
	private static RandomString randomString = new RandomString(32);
	
	// the data includes: the name of the player
	public static void createGame(String data, Session session) throws Exception {
		long gameCode = getGameId();
		Player creator = createPlayer(data, session, Player.CREATOR_ID);
		Game game = new Game(creator, gameCode);
		games.put(gameCode, game);
		creator.setGame(game); 
		Map<String, Object> feedback = getCreateGameFeedback(gameCode);
		sendMessage(session, Action.CREATE.getNumber(), feedback);
	}

	private static void sendMessage(Session session, int code, Map<String, Object> feedback) throws IOException, EncodeException {
		CheckersSessionManager.publish(getMessage(code, feedback), session);
	}

	private static Player createPlayer(String name, Session session, int playerId) {
		Player creator = new Player(session, name, playerId);
		players.put(getSessionToken(session), creator);
		return creator;
	}

	private static Map<String, Object> getCreateGameFeedback(long gameCode) {
		Map<String, Object> feedback = new HashMap<String, Object>();
		feedback.put("gameCode", gameCode);
		feedback.put("playerId", Player.CREATOR_ID);
		return feedback;
	}
	
	public static void joinGame(String data, Session session) throws Exception { 
		System.out.println(data);
		JsonObject jsonObject = parseToJsonObject(data);
		Game game = getGame(jsonObject);
		System.out.println("code: " + game.getId());
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
		sendMessage(game.getCreator().getSession(), Action.INFO.getNumber(), creatorFeedback);
		Map<String, Object> joinerFeedback = new HashMap<String, Object>();
		joinerFeedback.put("playerId", Player.JOINER_ID);
		sendMessage(session, Action.JOIN.getNumber(), joinerFeedback);
		updatePlay(game);
	}

	private static void updatePlay(Game game) throws IOException, EncodeException {
		Map<String, Object> board = getPlayFeedback(game);
		updatePlay(game.getCreator(), board);
		updatePlay(game.getJoiner(), board);
	}
	
	public static void updatePlay(Player player, Map<String, Object> board) throws IOException, EncodeException {
		System.out.println("updating the player...");
		sendMessage(player.getSession(), Action.STATE.getNumber(), board); 
	}

	public static Map<String, Object> getPlayFeedback(Game game) {
		Map<String, Object> board = new HashMap<String, Object>();
		board.put("turn", game.getPlayerInTurn().getId());
		board.put("checkers", game.getCheckers());
		return board;
	}

	private static Player createJoiner(Session session, JsonObject jsonObject, Game game) throws Exception { 
		if (game.isNew()) {
			String playerName = jsonObject.get("name").getAsString();
			System.out.println("name: " + playerName);
			Player joiner = createPlayer(playerName, session, Player.JOINER_ID);
			System.out.println("created: " + playerName);
			game.setJoiner(joiner);
			game.setStatus(Status.READY);
			System.out.println("creating joiner new");
			return joiner;
		}
		System.out.println("not allowed");
		throw new Exception("not allowed to join the game.");
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
		return players.get(token);
	}

	private static String getSessionToken(Session session) {
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
        throw new Exception("max games reached, try again later");
    }

	public static void leaveGame(Session session) throws IOException, EncodeException { 
		String token = getSessionToken(session);
		Player player = players.get(token);
		if (player.isCreator()) {
			deleteGame(player);
		} else {
			removeJoinerFromTheGame(player);
		}
	}

	private static void removeJoinerFromTheGame(Player joiner) throws IOException, EncodeException {
		Game game = joiner.getGame();
		game.setStatus(Game.Status.NEW);
		Player creator = game.getCreator();
		clearSessionAttributes(joiner.getSession());
		players.remove(getSessionToken(joiner.getSession()));
		sendMessage(creator.getSession(), Action.INFO.getNumber(), getInfoFeedback(String.format("%s left the game", joiner.getName()))); 
		sendMessage(joiner.getSession(), Action.INFO.getNumber(), getInfoFeedback("Closing the game"));
	}

	private static void deleteGame(Player creator) throws IOException, EncodeException {
		Game game = creator.getGame();
		Player joiner = game.getJoiner();
		games.remove(game.getId());
		clearSessionAttributes(creator.getSession());
		clearSessionAttributes(joiner.getSession());
		players.remove(getSessionToken(creator.getSession()));
		players.remove(getSessionToken(joiner.getSession()));
		sendMessage(creator.getSession(), Action.INFO.getNumber(), getInfoFeedback("game terminated"));
		sendMessage(joiner.getSession(), Action.CLOSE.getNumber(), getInfoFeedback("game terminated by the creator"));
	}

	private static void clearSessionAttributes(Session session) {
		session.getUserProperties().remove(Constants.PLAYER);
		session.getUserProperties().remove(Constants.TOKEN);
	}

	public static void restartGame(Session session) throws Exception {
		String token = getSessionToken(session);
		Player creator = players.get(token);
		Game game = creator.getGame();
		game.throwExceptionIfNotStartable();
		game.initBoard();
		game.setStarted();
		Map<String, Object> infoFeedback = getInfoFeedback("game restarted");
		sendMessage(creator.getSession(), Action.INFO.getNumber(), infoFeedback);
		sendMessage(game.getJoiner().getSession(), Action.INFO.getNumber(), infoFeedback);
		updatePlay(game);
	}

	public static void play(String data, Session session) throws Exception { 
		System.out.println("playing...");
		String token = getSessionToken(session);
		Player player = players.get(token);
		Game game = player.getGame();
		JsonArray plays = parseToJsonArray(data);
		game.updatePlay(player, plays);
	}

}
