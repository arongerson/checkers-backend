package com.aronek.checkers.entity;

import com.google.gson.annotations.Expose;

public class Checker {
	
	@Expose(serialize = true)
	private int row;
	
	@Expose(serialize = true)
	private int column;
	
	@Expose(serialize = true)
	private Piece piece;
	
	public Checker(int row, int column) {
		this.row = row;
		this.column = column;
	}
	
	public int getRow() {
		return row;
	}
	
	public int getColumn() {
		return column;
	}
	
	public void setPiece(Piece piece) {
		this.piece = piece;
	}
	
	public Piece getPiece() {
		return piece;
	}
}
