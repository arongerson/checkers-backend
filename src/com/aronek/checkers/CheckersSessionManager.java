package com.aronek.checkers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EncodeException;
import javax.websocket.Session;

import com.aronek.checkers.entity.Game;
import com.aronek.checkers.entity.Player;
import com.aronek.checkers.model.RandomString;

public final class CheckersSessionManager {

    private static final Lock LOCK = new ReentrantLock();
    private static final Set<Session> SESSIONS = new CopyOnWriteArraySet<>();
//    private static Map<String, Player> players = new HashMap<String, Player>();
//    private static RandomString randomString = new RandomString(32);

    private CheckersSessionManager() {
        throw new IllegalStateException(Constants.INSTANTIATION_NOT_ALLOWED);
    }

//    public static void publish(final Message message, final Session origin) {
//        assert !Objects.isNull(message) && !Objects.isNull(origin);
//
//        SESSIONS.stream().filter(session -> !session.equals(origin)).forEach(session -> {
//            try {
//                session.getBasicRemote().sendObject(message);
//            } catch (IOException | EncodeException e) {
//                e.printStackTrace();
//            }
//        });
//    }
    
	    public static void publish(final Message message, final Session origin) throws IOException, EncodeException { 
	        assert !Objects.isNull(message) && !Objects.isNull(origin);
	        origin.getBasicRemote().sendObject(message);
	        // System.out.println("feedback sent");
	    }

//    public static boolean register(final Session session) {
//        assert !Objects.isNull(session);
//
//        boolean result = false;
//        try {
//            LOCK.lock();
//            result = !SESSIONS.contains(session) && !SESSIONS.stream()
//                    .filter(elem -> ((String) elem.getUserProperties().get(Constants.USER_NAME_KEY)).equals((String) session.getUserProperties().get(Constants.USER_NAME_KEY)))
//                    .findFirst().isPresent() && SESSIONS.add(session);
//        } finally {
//            LOCK.unlock();
//        }
//        return result;
//    }
    
    
    
    public static void close(final Session session, final CloseCodes closeCode, final String message) {
        assert !Objects.isNull(session) && !Objects.isNull(closeCode);
        try {
            session.close(new CloseReason(closeCode, message));
        } catch (IOException e) {
            throw new RuntimeException("Unable to close session", e);
        }
    }

    public static boolean remove(final Session session) {
        assert !Objects.isNull(session);
        return SESSIONS.remove(session);
    }
}
