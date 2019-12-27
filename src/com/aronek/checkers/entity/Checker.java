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

	public boolean isLastRow(int lastRowIndex) {
		return row == lastRowIndex;
	}

	public boolean isFirstRow() {
		return row == 0;
	}
	
	public boolean isEmpty() {
		return piece == null;
	}

	public boolean hasEmptyCheckersAround(Game game, int rowMoveId) {
		Checker leftForwardChecker = getLeftForwardChecker(game, rowMoveId);
		Checker rightForwardChecker = getRightForwardChecker(game, rowMoveId);
		Checker leftBackwardChecker = getLeftBackwardChecker(game, rowMoveId);
		Checker rightBackwardChecker = getRightBackwardChecker(game, rowMoveId);
		return isEmptyChecker(leftForwardChecker) || isEmptyChecker(rightForwardChecker) || 
				isEmptyChecker(leftBackwardChecker) || isEmptyChecker(rightBackwardChecker);
	}

	public boolean canKingCapture(Game game, int rowMoveId) {
		Checker leftForwardChecker = getLeftForwardChecker(game, rowMoveId);
		Checker rightForwardChecker = getRightForwardChecker(game, rowMoveId);
		Checker leftBackwardChecker = getLeftBackwardChecker(game, rowMoveId);
		Checker rightBackwardChecker = getRightBackwardChecker(game, rowMoveId);
		return isLeftForwardCapturable(game, leftForwardChecker, rowMoveId) || 
				   isRightForwardCapturable(game, rightForwardChecker, rowMoveId) || 
				   isLeftBackwardCapturable(game, leftBackwardChecker, rowMoveId) ||
				   isRightBackwardCapturable(game, rightBackwardChecker, rowMoveId);
	}

	public boolean hasEmptyForwardCheckers(Game game, int rowMoveId) {
		Checker leftForwardChecker = getLeftForwardChecker(game, rowMoveId);
		Checker rightForwardChecker = getRightForwardChecker(game, rowMoveId);
		return isEmptyChecker(leftForwardChecker) || isEmptyChecker(rightForwardChecker);
	}

	public boolean canOrdinaryCapture(Game game, int rowMoveId) {
		Checker leftForwardChecker = getLeftForwardChecker(game, rowMoveId);
		Checker rightForwardChecker = getRightForwardChecker(game, rowMoveId);
		Checker leftBackwardChecker = getLeftBackwardChecker(game, rowMoveId);
		Checker rightBackwardChecker = getRightBackwardChecker(game, rowMoveId);
		return isLeftForwardCapturable(game, leftForwardChecker, rowMoveId) || 
			   isRightForwardCapturable(game, rightForwardChecker, rowMoveId) || 
			   isLeftBackwardCapturable(game, leftBackwardChecker, rowMoveId) ||
			   isRightBackwardCapturable(game, rightBackwardChecker, rowMoveId); 
	}

	private boolean isLeftForwardCapturable(Game game, Checker leftForwardChecker, int rowMoveId) {
		if (leftForwardChecker != null) {
			Checker fartherChecker = leftForwardChecker.getLeftForwardChecker(game, rowMoveId);
			return leftForwardChecker.hasOpponentPiece(game.getPlayerInTurn()) && isEmptyChecker(fartherChecker);
		}
		return false;
	}
	
	private boolean isRightForwardCapturable(Game game, Checker rightForwardChecker, int rowMoveId) {
		if (rightForwardChecker != null) {
			Checker fartherChecker = rightForwardChecker.getRightForwardChecker(game, rowMoveId);
			return rightForwardChecker.hasOpponentPiece(game.getPlayerInTurn()) && isEmptyChecker(fartherChecker);
		}
		return false;
	}
	
	private boolean isLeftBackwardCapturable(Game game, Checker leftBackwardChecker, int rowMoveId) {
		if (leftBackwardChecker != null) {
			Checker fartherChecker = leftBackwardChecker.getLeftBackwardChecker(game, rowMoveId);
			return leftBackwardChecker.hasOpponentPiece(game.getPlayerInTurn()) && isEmptyChecker(fartherChecker);
		}
		return false;
	}
	
	private boolean isRightBackwardCapturable(Game game, Checker rightBackwardChecker, int rowMoveId) {
		if (rightBackwardChecker != null) {
			Checker fartherChecker = rightBackwardChecker.getRightBackwardChecker(game, rowMoveId);
			return rightBackwardChecker.hasOpponentPiece(game.getPlayerInTurn()) && isEmptyChecker(fartherChecker);
		}
		return false;
	}
	
	public Checker getLeftForwardChecker(Game game, int rowMoveId) {
		return game.getChecker(row + rowMoveId, column - 1);
	} 
	
	public Checker getRightForwardChecker(Game game, int rowMoveId) {
		return game.getChecker(row + rowMoveId, column + 1);
	}
	
	public Checker getLeftBackwardChecker(Game game, int rowMoveId) {
		return game.getChecker(row - rowMoveId, column - 1);
	}
	
	public Checker getRightBackwardChecker(Game game, int rowMoveId) {
		return game.getChecker(row - rowMoveId, column + 1);
	}
	
	public Checker getNextOccupiedLeftForwardChecker(Game game, Checker checker, int rowMoveId) {
		Checker nextChecker = checker.getLeftForwardChecker(game, rowMoveId);
		while (nextChecker != null) {
			if (!nextChecker.isEmpty()) {
				return nextChecker;
			}
			nextChecker = nextChecker.getLeftForwardChecker(game, rowMoveId);
		}
		return null;
	}
	
	public Checker getNextOccupiedRightForwardChecker(Game game, Checker checker, int rowMoveId) {
		Checker nextChecker = checker.getRightForwardChecker(game, rowMoveId);
		while (nextChecker != null) {
			if (!nextChecker.isEmpty()) {
				return nextChecker;
			}
			nextChecker = nextChecker.getRightForwardChecker(game, rowMoveId);
		}
		return null;
	}
	
	public Checker getNextOccupiedLeftBackwardChecker(Game game, Checker checker, int rowMoveId) {
		Checker nextChecker = checker.getLeftBackwardChecker(game, rowMoveId);
		while (nextChecker != null) {
			if (!nextChecker.isEmpty()) {
				return nextChecker;
			}
			nextChecker = nextChecker.getLeftBackwardChecker(game, rowMoveId);
		}
		return null;
	}
	
	public Checker getNextOccupiedRightBackwardChecker(Game game, Checker checker, int rowMoveId) {
		Checker nextChecker = checker.getRightBackwardChecker(game, rowMoveId);
		while (nextChecker != null) {
			if (!nextChecker.isEmpty()) {
				return nextChecker;
			}
			nextChecker = nextChecker.getRightBackwardChecker(game, rowMoveId);
		}
		return null;
	}
	
	private boolean hasOpponentPiece(Player playerInTurn) {
		Piece piece = getPiece();
		return !isEmpty() && piece.getOwner() != playerInTurn;
	} 
	
	private boolean isEmptyChecker(Checker checker) {
		return checker != null && checker.isEmpty();
	}
}
