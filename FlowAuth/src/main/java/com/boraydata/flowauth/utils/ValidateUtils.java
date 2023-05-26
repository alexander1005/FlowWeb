package com.boraydata.flowauth.utils;

import java.util.regex.Pattern;

public class ValidateUtils {

    public static Boolean validateName(String name) {
        return Pattern.matches("[\\w-]+", name);
    }
}
