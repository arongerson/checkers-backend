package com.aronek.checkers.entity;

import java.io.Serializable;

import javax.websocket.Session;

import com.google.gson.annotations.Expose;

public class Player implements Serializable {

	private static final long serialVersionUID = 1L;
	// 1 and -1 simplify the login since the creator plays in the direction of increasing row numbers
	// while the joiner plays in the order of decreasing row numbers
	public static final int CREATOR_ID = 1;
	public static final int JOINER_ID = -1;
	
	@Expose(serialize = true)
	private String name;
	
	@Expose(serialize = true)
	private int id;
	
	private transient Session session;
	private Game game;
	
	public Player() {}
	
	public Player(Session session, String name, int id) {
		this.session = session;
		this.name = name;
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
	
	public void setGame(Game game) {
		this.game = game;
	}
	
	public Game getGame() {
		return game;
	}

	public Player getOtherPlayer() {
		return game.getOtherPlayer(this);
	}

	public boolean isCreator() {
		return game.isCreator(this);
	}
	
}
