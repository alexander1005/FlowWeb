package com.boraydata.flow.install.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class StudioApplicationConfig extends AuthApplicationConfig {
    private final Map<String, Object> properties = new LinkedHashMap<>();

    public Map<String, Object> getProperties() {
        return properties;
    }


}
