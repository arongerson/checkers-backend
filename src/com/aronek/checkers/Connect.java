package com.aronek.checkers;

import java.io.IOException;
import java.util.Map;

import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.aronek.checkers.entity.Checkers;
import com.aronek.checkers.entity.Game;
import com.aronek.checkers.entity.Player;
import com.aronek.checkers.model.Action;
import com.aronek.checkers.model.CheckerException;

@ServerEndpoint(value = "/connect/{token}", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public final class Connect {
	
	private static org.apache.log4j.Logger log = Logger.getLogger(Connect.class);
    
    @OnOpen
    public void onOpen(
    		@PathParam("token") final String token, 
    		final Session session, 
    		EndpointConfig endConfig) throws Exception { 
    	Player player = Checkers.getPlayer(token);
    	session.setMaxIdleTimeout(10* 60 * 1000);
    	if (!playerExists(player)) {
    		createNewToken(session);
    		log.info(String.format("new connection from"));
    	} else {
    		log.info(String.format("the player reconnected."));
    		updateSessionWithPreviousToken(token, session, player);
    	}
    }

	private void updateSessionWithPreviousToken(final String token, final Session session, Player player)
			throws IOException, EncodeException {
		updateSession(token, session, player);
		if (playableGameExists(player)) {
			notifyTheOtherPlayer(player);
		} else {
			inviteThePlayerBack(session);
		}
		if (player.getGame() != null) {
    		sendCurrentBoardStatus(player);
    	}
	}

	private void inviteThePlayerBack(final Session session) throws IOException, EncodeException {
		CheckersSessionManager.publish(
				new Message(Action.INFO.getNumber(), "welcome back"), session);
	}

	private void notifyTheOtherPlayer(Player player) throws IOException, EncodeException {
		Player otherPlayer = player.getOtherPlayer();
		CheckersSessionManager.publish(
				new Message(Action.OTHER_RECONNECT.getNumber(), player.getName() + " is back"), otherPlayer.getSession());
	}

	private void sendCurrentBoardStatus(Player player) throws IOException, EncodeException {
		Map<String, Object> board = Checkers.getPlayFeedback(player.getGame());
		Checkers.updateStatus(player, board);
	}

	private boolean playableGameExists(Player player) {
		Game.Status status = player.getGame().getStatus();
		return player.getGame() != null && status != Game.Status.NEW && status != Game.Status.TERMINATED;
	}

	private void updateSession(final String token, final Session session, Player player) {
		session.getUserProperties().put(Constants.TOKEN, token);
		session.getUserProperties().put(Constants.PLAYER, player);
		player.setSession(session);
		System.out.println("new session set");
	}

	private void createNewToken(final Session session) throws Exception, IOException, EncodeException {
		String tokenId = Checkers.generatePlayerToken();
		session.getUserProperties().put(Constants.TOKEN, tokenId);
		CheckersSessionManager.publish(new Message(Action.CONNECT.getNumber(), tokenId), session);
	}

	private boolean playerExists(Player player) {
		return player != null;
	}
 
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        if (throwable instanceof RegistrationFailedException) {
            CheckersSessionManager.close(session, CloseCodes.VIOLATED_POLICY, throwable.getMessage());
        } else {
        	log.fatal(String.format("websocket error inside onError: %s", throwable.getMessage()));
        }
    }
 
    @OnMessage
    public void onMessage(final Message message, final Session session) throws Exception { 
    	try {
    		processMessage(message, session);
    	} catch(CheckerException exception) {
    		exception.printStackTrace();
    		log.fatal(String.format("websocket error inside onMessage: %s", exception.getMessage())); 
    	}
    }
    
    private void processMessage(Message message, Session session) throws Exception { 
    	int code = message.getCode();
        if (code == Action.CREATE.getNumber()) {
        	Checkers.createGame(message.getData(), session);
        	log.info(String.format("game created"));
        } else if (code == Action.JOIN.getNumber()) {
        	Checkers.joinGame(message.getData(), session);
        	log.info(String.format("game joined"));
        } else if (code == Action.STATE.getNumber()) {
        	Checkers.getGameState(session);
        } else if (code == Action.REGISTER.getNumber()) {
        } else if (code == Action.LOGIN.getNumber()) {
        } else if (code == Action.CHAT.getNumber()) {
        	Checkers.chat(message.getData(), session);
        } else if (code == Action.PLAY.getNumber()) {
        	Checkers.play(message.getData(), session);
        } else if (code == Action.LEAVE.getNumber()) {
        	Checkers.leaveGame(session, null);
        } else if (code == Action.RESTART.getNumber()) {
        	Checkers.restartGame(session);
        } else if (code == Action.CONNECT.getNumber()) {
        } else {
        	CheckersSessionManager.publish(new Message(Action.ERROR.getNumber(), "invalid code"), session);
        }
	}

	@OnClose
    public void onClose(final Session session) throws IOException, InterruptedException {  
    	session.close();
		log.info(String.format("session closed"));
		Checkers.cleanUpGame(session);
		System.out.println("session closed: " + Checkers.getSessionToken(session));
    }
 
    private static final class RegistrationFailedException extends RuntimeException {
 
        private static final long serialVersionUID = 1L;
 
    }
}
