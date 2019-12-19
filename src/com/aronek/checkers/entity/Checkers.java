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
import com.aronek.checkers.model.Action;
import com.aronek.checkers.model.RandomString;
import com.google.gson.Gson;
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
		Player creator = new Player(session, data, Player.CREATOR_ID);
		players.put(getSessionToken(session), creator);
		Game game = new Game(creator, gameCode);
		games.put(gameCode, game);
		creator.setGame(game); 
		Map<String, Object> feedback = new HashMap<String, Object>();
		feedback.put("gameCode", gameCode);
		feedback.put("playerId", Player.CREATOR_ID);
		CheckersSessionManager.publish(getMessage(Action.CREATE.getNumber(), feedback), session);
	}
	
	public static void joinGame(String data, Session session) throws IOException, EncodeException { 
		JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
		String playerName = jsonObject.get("name").getAsString();
		long gameCode = jsonObject.get("code").getAsLong();
		Game game = games.get(gameCode);
		Objects.requireNonNull(game);
		Player joiner = new Player(session, playerName, Player.JOINER_ID);
		players.put(getSessionToken(session), joiner);
		game.setJoiner(joiner);
		joiner.setGame(game); 
		Player creator = game.getOtherPlayer(joiner);
		Map<String, Object> creatorFeedback = new HashMap<String, Object>();
		creatorFeedback.put("info", String.format("%s has joined the game", playerName));
		CheckersSessionManager.publish(getMessage(Action.INFO.getNumber(), creatorFeedback), creator.getSession());
		Map<String, Object> joinerFeedback = new HashMap<String, Object>();
		joinerFeedback.put("playerId", Player.JOINER_ID);
		CheckersSessionManager.publish(getMessage(Action.JOIN.getNumber(), joinerFeedback), session);
		
		// create game board and update the players with new status
	}
	
	private static Message getMessage(int code, Object data) {
		Gson gson = new Gson();
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

}
