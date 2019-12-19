package com.aronek.checkers.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.EncodeException;
import javax.websocket.Session;


import com.aronek.checkers.CheckersSessionManager;
import com.aronek.checkers.Constants;
import com.aronek.checkers.Message;
import com.aronek.checkers.model.Action;
import com.aronek.checkers.model.RandomString;

public class Game {
	
	private long id;
	private Player creator;
	private Player joiner;
	private Player playerInTurn;
	private Checker[][] checkers;
	private Status status;
	private List<Chat> chats;
	
	
	public Game(Player creator, long id) {
		this.id = id;
		this.creator = creator;
	}


	public long getId() {
		return id;
	}


	public void setId(long id) {
		this.id = id;
	}


	public Player getCreator() {
		return creator;
	}


	public void setCreator(Player creator) {
		this.creator = creator;
	}


	public Player getJoiner() {
		return joiner;
	}


	public void setJoiner(Player joiner) {
		this.joiner = joiner;
	}


	public Player getPlayerInTurn() {
		return playerInTurn;
	}


	public void setPlayerInTurn(Player playerInTurn) {
		this.playerInTurn = playerInTurn;
	}


	public Checker[][] getCheckers() {
		return checkers;
	}


	public void setCheckers(Checker[][] checkers) {
		this.checkers = checkers;
	}


	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}


	public List<Chat> getChats() {
		return chats;
	}


	public void setChats(List<Chat> chats) {
		this.chats = chats;
	}
	
	public Player getOtherPlayer(Player player) {
		if (player == creator) {
			return joiner;
		}
		return creator;
	}
	
	public enum Status {
		NEW, READY, STARTED, OVER
	}
	
}
