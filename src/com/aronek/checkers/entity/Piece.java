package com.aronek.checkers.entity;

public class Piece {
	private Checker checker;
	private Player owner;
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
