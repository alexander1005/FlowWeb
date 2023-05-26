package com.boraydata.flowauth.utils;

import java.util.Arrays;

public class ErrorUtils {

//    public static String getErrorMessage(Throwable e) {
//        StringBuilder errorStr = new StringBuilder();
//        errorStr.append(e.getClass()).append(": ").append(e.getMessage()).append("\n");
//        Arrays.stream(e.getStackTrace()).forEach(s -> errorStr.append("at ").append(s).append("\n"));
//        Throwable cause = e.getCause();
//        if (cause != null) {
//            errorStr.append("Caused by: ").append(cause.getClass()).append(": ").append(cause.getMessage()).append("\n");
//            Arrays.stream(cause.getStackTrace()).forEach(s -> errorStr.append("at ").append(s).append("\n"));
//        }
//        return errorStr.toString();
//    }

    public static String getErrorMessage(Throwable e) {
        StringBuilder errorStr = new StringBuilder();
        errorStr.append(e.getClass()).append(": ").append(e.getMessage()).append("\n");
        Arrays.stream(e.getStackTrace()).forEach(s -> errorStr.append("at ").append(s).append("\n"));
        Throwable cause = e.getCause();
        getErrorMessage(cause, errorStr);
        return errorStr.toString();
    }

    private static void getErrorMessage(Throwable cause, StringBuilder errorStr) {
        if (cause != null) {
            errorStr.append("\nCaused by: ").append(cause.getClass()).append(": ").append(cause.getMessage()).append("\n");
            Arrays.stream(cause.getStackTrace()).forEach(s -> errorStr.append("at ").append(s).append("\n"));
            getErrorMessage(cause.getCause(), errorStr);
        }
    }

}
