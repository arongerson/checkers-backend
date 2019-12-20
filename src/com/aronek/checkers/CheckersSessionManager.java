package com.aronek.checkers;

import java.io.IOException;
import java.util.Objects;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.Session;

public final class CheckersSessionManager {

	private CheckersSessionManager() {
	}

	public static void publish(final Message message, final Session origin) throws IOException, EncodeException {
		assert !Objects.isNull(message) && !Objects.isNull(origin);
		origin.getBasicRemote().sendObject(message);
	}

	public static void close(final Session session, final CloseCodes closeCode, final String message) {
		assert !Objects.isNull(session) && !Objects.isNull(closeCode);
		try {
			session.close(new CloseReason(closeCode, message));
		} catch (IOException e) {
			throw new RuntimeException("Unable to close session", e);
		}
	}

}
