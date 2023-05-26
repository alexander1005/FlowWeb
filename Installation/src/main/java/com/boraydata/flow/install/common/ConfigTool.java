package com.boraydata.flow.install.common;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigTool {
    public static Map<String, Object> config(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    public static Map<String, Object> config(Map<String, Object> map, String key, Object value) {
        map.put(key, value);
        return map;
    }
}
