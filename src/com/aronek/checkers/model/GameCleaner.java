package com.aronek.checkers.model;

import java.io.IOException;
import java.util.TimerTask;

import javax.websocket.EncodeException;

import org.apache.log4j.Logger;

import com.aronek.checkers.Connect;
import com.aronek.checkers.entity.Checkers;
import com.aronek.checkers.entity.Player;

public class GameCleaner extends TimerTask { 
	
	private static org.apache.log4j.Logger log = Logger.getLogger(Connect.class);
	private Player player;
	private String token;
	
    public GameCleaner(Player player, String token) {
		this.player = player;
		this.token = token;
	}

	public void run() { 
    	log.info("Cleaning idle games");
    	if (player != null && !player.getSession().isOpen()) {
			System.out.println("clean up the game");
			try {
				Checkers.leaveGame(player.getSession(), token);
			} catch (IOException | EncodeException | CheckerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.info(String.format("Player cleaned up"));
		}
    	synchronized(player) {
    		player.notify();
    	}
    }  
} 
