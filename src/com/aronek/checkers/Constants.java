package com.aronek.checkers;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Constants {

    static final String INSTANTIATION_NOT_ALLOWED = "Instantiation not allowed";
    static final String CODE_KEY = "code";
    static final String DATA_KEY = "data";
    static final ObjectMapper MAPPER = new ObjectMapper();
	public static final String PLAYER = "player";
	public static final String TOKEN = "token";

    private Constants() {
        throw new IllegalStateException(INSTANTIATION_NOT_ALLOWED);
    }
}
