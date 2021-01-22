package com.aronek.checkers;

import java.io.IOException;
import java.util.Objects;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import org.apache.log4j.Logger;

import javax.websocket.EncodeException;
import javax.websocket.Session;

public final class CheckersSessionManager {
	
	private static org.apache.log4j.Logger log = Logger.getLogger(CheckersSessionManager.class);

	private CheckersSessionManager() {
	}

	public static void publish(final Message message, final Session origin) throws IOException, EncodeException {
		assert !Objects.isNull(message) && !Objects.isNull(origin);
		if (origin.isOpen()) {
			origin.getBasicRemote().sendObject(message);
		}
	}

	public static void close(final Session session, final CloseCodes closeCode, final String message) {
		try {
			session.close(new CloseReason(closeCode, message));
		} catch (IOException e) {
			log.fatal(String.format("Unable to close session: %s", e.getMessage()));
			throw new RuntimeException("", e);
		}
	}

}
