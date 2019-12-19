package com.aronek.checkers;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Message {

    @JsonProperty("code")
    private final int code;
    
    @JsonProperty("data")
    private final String data;

    @JsonCreator
    public Message(@JsonProperty("code") final int code, @JsonProperty("data") final String data) {
        Objects.requireNonNull(code);
        Objects.requireNonNull(data);
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return this.code;
    }

    public String getData() {
        return this.data;
    }
}
