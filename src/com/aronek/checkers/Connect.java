package com.aronek.checkers;

import javax.websocket.CloseReason.CloseCodes;
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
    	if (player == null) {
    		String tokenId = Checkers.generatePlayerToken();
    		session.getUserProperties().put(Constants.TOKEN, tokenId);
    		CheckersSessionManager.publish(new Message(Action.CONNECT.getNumber(), tokenId), session);
    	} else {
    		// update the game status to the connected player
    		CheckersSessionManager.publish(
    				new Message(Action.PLAY.getNumber(), "welcome back"), session);
    		// update session properties
    		session.getUserProperties().put(Constants.TOKEN, token);
    		session.getUserProperties().put(Constants.PLAYER, player);
    		// inform the other player of the connection
    		player.setSession(session);
    		Player otherPlayer = player.getOtherPlayer();
    		CheckersSessionManager.publish(
    				new Message(Action.OTHER_RECONNECT.getNumber(), player.getName() + " is back"), otherPlayer.getSession());
    	}
    }
 
    @OnError
    public void onError(final Session session, final Throwable throwable) {
        if (throwable instanceof RegistrationFailedException) {
            CheckersSessionManager.close(session, CloseCodes.VIOLATED_POLICY, throwable.getMessage());
        }
    }
 
    @OnMessage
    public void onMessage(final Message message, final Session session) throws Exception { 
        // CheckersSessionManager.publish(message, session);
    	int code = message.getCode();
        if (code == Action.CREATE.getNumber()) {
        	Checkers.createGame(message.getData(), session);
        } else if (code == Action.JOIN.getNumber()) {
        	Checkers.joinGame(message.getData(), session);
        } else if (code == Action.REGISTER.getNumber()) {
        	// future version
        } else if (code == Action.LOGIN.getNumber()) {
        	// future version
        } else if (code == Action.CHAT.getNumber()) {
        	
        } else if (code == Action.PLAY.getNumber()) {
        } else if (code == Action.LEAVE.getNumber()) {
        } else if (code == Action.RESTART.getNumber()) {
        } else if (code == Action.CONNECT.getNumber()) {
        } else if (code == Action.OTHER_RECONNECT.getNumber()) {
        } else {
        	CheckersSessionManager.publish(new Message(Action.ERROR.getNumber(), "invalid code"), session);
        }
    }
    
    @OnClose
    public void onClose(final Session session) {
    	System.out.println("closing the connection");
        if (CheckersSessionManager.remove(session)) {
        }
    }
 
    private static final class RegistrationFailedException extends RuntimeException {
 
        private static final long serialVersionUID = 1L;
 
        public RegistrationFailedException(final String message) {
            super(message);
        }
    }
}
