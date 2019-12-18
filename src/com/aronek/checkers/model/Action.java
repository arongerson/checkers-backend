package com.aronek.checkers.model;

public enum Action {
	
	REGISTER(1), LOGIN(2), CHAT(3), PLAY(4), JOIN(5), LEAVE(6), RESTART(7), CREATE(8);
	
	private final int number;
	
	private Action(final int number) {
		this.number = number;
	}
	
	public final int getNumber() {
        return this.number;
    }
}