package com.aronek.checkers;

import com.fasterxml.jackson.databind.ObjectMapper;

final class Constants {

    static final String INSTANTIATION_NOT_ALLOWED = "Instantiation not allowed";
    static final String CODE_KEY = "code";
    static final String DATA_KEY = "data";
    static final ObjectMapper MAPPER = new ObjectMapper();

    private Constants() {
        throw new IllegalStateException(INSTANTIATION_NOT_ALLOWED);
    }
}
