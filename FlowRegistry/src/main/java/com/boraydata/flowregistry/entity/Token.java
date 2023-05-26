package com.boraydata.flowregistry.entity;

public class Token {
    private String user;
    private long current;

    public Token(String user, long current) {
        this.user = user;
        this.current = current;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }
}
