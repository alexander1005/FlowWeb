package com.boraydata.flowregistry.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertiesUtils {

    public static long EXPIRE;

    @Value("${security.auth.expire}")
    public void setExpire(long expire) {
        EXPIRE = expire;
    }
}
