package com.boraydata.flow.install.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class AuthApplicationConfig {

    private final Map<String, Object> server = new LinkedHashMap<>();
    private final Map<String, Object> spring = new LinkedHashMap<>();
    private final Map<String, Object> security = new LinkedHashMap<>();
    private final Map<String, Object> quartz = new LinkedHashMap<>();
    private final Map<String, Object> registry = new LinkedHashMap<>();

    public Map<String, Object> getServer() {
        return server;
    }

    public Map<String, Object> getSpring() {
        return spring;
    }

    public Map<String, Object> getSecurity() {
        return security;
    }

    public Map<String, Object> getQuartz() {
        return quartz;
    }

    public Map<String, Object> getRegistry() {
        return registry;
    }
}
