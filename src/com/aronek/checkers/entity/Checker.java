package com.aronek.checkers.entity;

public class Checker {
	private int row;
	private int column;
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
		
	}
	
	public Piece getPiece() {
		return piece;
	}
}
