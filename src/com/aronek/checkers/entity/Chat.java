package com.aronek.checkers.entity;

import java.util.Date;

public class Chat {
	
	private Player from;
	private Player to;
	private String content;
	private Date date;
	
	public Chat(String content) {
		
	}
	
	public String getContent() {
		return content;
	}
	
	public Date getDate() {
		return date;
	}

	public Player getFrom() {
		return from;
	}

	public void setFrom(Player from) {
		this.from = from;
	}

	public Player getTo() {
		return to;
	}

	public void setTo(Player to) {
		this.to = to;
	}
	
}
