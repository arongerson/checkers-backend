package com.aronek.checkers.model;

public enum Action {
	
	REGISTER(1), LOGIN(2), CHAT(3), PLAY(4), JOIN(5), LEAVE(6), RESTART(7), 
	CREATE(8), ERROR(9), CONNECT(10), OTHER_RECONNECT(11), INFO(12), CLOSE(13), STATE(14), OVER(15), OTHER_CLOSE(16),
	ACTION_OTHER_JOINED(17), UPDATE_RULE(18), ACCEPT(19);   
	
	private final int number;
	
	private Action(final int number) {
		this.number = number;
	}
	
	public final int getNumber() {
        return this.number;
    }
}
