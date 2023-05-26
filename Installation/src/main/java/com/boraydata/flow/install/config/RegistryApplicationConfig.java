package com.boraydata.flow.install.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegistryApplicationConfig extends AuthApplicationConfig {
    private final Map<String, Object> monitor = new LinkedHashMap<>();

    public Map<String, Object> getMonitor() {
        return monitor;
    }
}
