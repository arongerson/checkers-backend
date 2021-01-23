package com.aronek.checkers.entity;

import com.google.gson.annotations.Expose;

public class Rules {
	@Expose(serialize = true)
	public boolean canPieceCaptureBackwards = true;
	@Expose(serialize = true)
	public boolean canKingMoveMoreThanOneStep = true;
	@Expose(serialize = true)
	public boolean shouldPieceContinueCapturingAfterFarthestRow = true;
	@Expose(serialize = true)
	public boolean shouldCaptureWhenPossible = true;
	@Expose(serialize = true)
	public boolean shouldCaptureMaxPossible = true;
	@Expose(serialize = true)
	public boolean shouldDiscardCapturedPieceMomentarily = true;
}
