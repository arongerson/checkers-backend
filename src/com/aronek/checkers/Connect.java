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

import com.aronek.checkers.entity.Checkers;
import com.aronek.checkers.entity.Game;
import com.aronek.checkers.entity.Player;
import com.aronek.checkers.model.Action;

@ServerEndpoint(value = "/connect/{token}", encoders = MessageEncoder.class, decoders = MessageDecoder.class)
public final class Connect {
    
    @OnOpen
    public void onOpen(
    		@PathParam("token") final String token, 
    		final Session session, 
    		EndpointConfig endConfig) throws Exception { 
    	Player player = Checkers.getPlayer(token);
    	if (!playerExists(player)) {
    		createNewToken(session);
    	} else {
    		updateSessionWithPreviousToken(token, session, player);
    	}
    }

	private void updateSessionWithPreviousToken(final String token, final Session session, Player player)
			throws IOException, EncodeException {
		updateSession(token, session, player);
		if (playableGameExists(player)) {
			sendCurrentBoardStatus(player);
			notifyTheOtherPlayer(player);
		} else {
			inviteThePlayerBack(session);
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
		Checkers.updatePlay(player, board);
	}

	private boolean playableGameExists(Player player) {
		return player.getGame() != null && player.getGame().getStatus() != Game.Status.NEW;
	}

	private void updateSession(final String token, final Session session, Player player) {
		session.getUserProperties().put(Constants.TOKEN, token);
		session.getUserProperties().put(Constants.PLAYER, player);
		player.setSession(session);
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
        	System.out.println("We have an error");
            CheckersSessionManager.close(session, CloseCodes.VIOLATED_POLICY, throwable.getMessage());
        } else {
        	System.out.println("We have an error: " + throwable.getMessage());
        	throwable.printStackTrace();
        }
    }
 
    @OnMessage
    public void onMessage(final Message message, final Session session) throws Exception { 
    	int code = message.getCode();
        if (code == Action.CREATE.getNumber()) {
        	Checkers.createGame(message.getData(), session);
        } else if (code == Action.JOIN.getNumber()) {
        	Checkers.joinGame(message.getData(), session);
        } else if (code == Action.REGISTER.getNumber()) {
        } else if (code == Action.LOGIN.getNumber()) {
        } else if (code == Action.CHAT.getNumber()) {
        } else if (code == Action.PLAY.getNumber()) {
        	Checkers.play(message.getData(), session);
        } else if (code == Action.LEAVE.getNumber()) {
        	Checkers.leaveGame(session);
        } else if (code == Action.RESTART.getNumber()) {
        	Checkers.restartGame(session);
        } else if (code == Action.CONNECT.getNumber()) {
        } else {
        	CheckersSessionManager.publish(new Message(Action.ERROR.getNumber(), "invalid code"), session);
        }
    }
    
    @OnClose
    public void onClose(final Session session) {
    	System.out.println("closing the connection");
    }
 
    private static final class RegistrationFailedException extends RuntimeException {
 
        private static final long serialVersionUID = 1L;
 
    }
}
