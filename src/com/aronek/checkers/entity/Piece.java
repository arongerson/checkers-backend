package com.aronek.checkers.entity;

import com.google.gson.annotations.Expose;

public class Piece {
	
	private Checker checker;
	
	@Expose(serialize = true)
	private Player owner;
	
	@Expose(serialize = true)
	private Type type;
	
	public Piece(Player owner) {
		this.owner = owner;
	}
	
	public Checker getChecker() {
		return checker;
	}
	public void setChecker(Checker checker) {
		this.checker = checker;
	}
	public Player getOwner() {
		return owner;
	}
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	public enum Type {
		NORMAL, KING
	}
	
}
