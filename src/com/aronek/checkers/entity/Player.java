package com.aronek.checkers.entity;

import javax.websocket.Session;

public class Player {
	
	private String name;
	private Session session;
	private Game game;
	
	public Player(Session session, Game game, String name) {
		this.session = session;
		this.name = name;
		this.game = game;
	}
	
	public String getName() {
		return name;
	}

	public Session getSession() {
		return session;
	}
	
	public Game getGame() {
		return game;
	}
	
}
