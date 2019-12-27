package com.aronek.checkers.entity;

import com.google.gson.annotations.Expose;

public class Piece {
	
	private Checker checker;
	
	@Expose(serialize = true)
	private Player owner;
	
	@Expose(serialize = true)
	private int type;
	
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
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public boolean isAtLastRow(int lastRowIndex) {
		System.out.println("owner: " + owner.getId() + ", " + checker.getRow() + ", " + lastRowIndex);
		if (owner.isCreator() && checker.isLastRow(lastRowIndex)) {
			return true;
		} else if (!owner.isCreator() && checker.isFirstRow()) {
			return true;
		}
		return false;
	}
	
	public boolean isPiecePlayable(Game game) {
		int ownerId = owner.getId();
		if (isKing()) {
			 return checker.hasEmptyCheckersAround(game, ownerId) || checker.canKingCapture(game, ownerId);
		} else {
			return checker.hasEmptyForwardCheckers(game, ownerId) || checker.canOrdinaryCapture(game, ownerId);
		}
	}
	
	private boolean isKing() {
		return type == Type.KING.getNumber();
	}
	
	public enum Type {
		
		NORMAL(1), KING(2);
		
		private final int number;
		
		private Type(final int number) {
			this.number = number;
		}
		
		public final int getNumber() {
	        return this.number;
	    }
	}
	
}
