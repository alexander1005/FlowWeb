package com.boraydata.workflow.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceUtils {
    private final static String REGREX = "[\\u00B2\\u00B3\\u00B9\\u00BC-\\u00BE\\u09F4-\\u09F9\\u0B72-\\u0B77\\u0BF0-\\u0BF2\\u0C78-\\u0C7E\\u0D58-\\u0D5E\\u0D70-\\u0D78\\u0F2A-\\u0F33\\u1369-\\u137C\\u17F0-\\u17F9\\u19DA\\u2070\\u2074-\\u2079\\u2080-\\u2089\\u2150-\\u215F\\u2189\\u2460-\\u249B\\u24EA-\\u24FF\\u2776-\\u2793\\u2CFD\\u3192-\\u3195\\u3220-\\u3229\\u3248-\\u324F\\u3251-\\u325F\\u3280-\\u3289\\u32B1-\\u32BF\\uA830-\\uA835\\\\U00010107-\\\\U00010133\\\\U00010175-\\\\U00010178\\\\U0001018A\\\\U0001018B\\\\U000102E1-\\\\U000102FB\\\\U00010320-\\\\U00010323\\\\U00010858-\\\\U0001085F\\\\U00010879-\\\\U0001087F\\\\U000108A7-\\\\U000108AF\\\\U000108FB-\\\\U000108FF\\\\U00010916-\\\\U0001091B\\\\U000109BC\\\\U000109BD\\\\U000109C0-\\\\U000109CF\\\\U000109D2-\\\\U000109FF\\\\U00010A40-\\\\U00010A47\\\\U00010A7D\\\\U00010A7E\\\\U00010A9D-\\\\U00010A9F\\\\U00010AEB-\\\\U00010AEF\\\\U00010B58-\\\\U00010B5F\\\\U00010B78-\\\\U00010B7F\\\\U00010BA9-\\\\U00010BAF\\\\U00010CFA-\\\\U00010CFF\\\\U00010E60-\\\\U00010E7E\\\\U00011052-\\\\U00011065\\\\U000111E1-\\\\U000111F4\\\\U0001173A\\\\U0001173B\\\\U000118EA-\\\\U000118F2\\\\U00011C5A-\\\\U00011C6C\\\\U00016B5B-\\\\U00016B61\\\\U0001D360-\\\\U0001D371\\\\U0001E8C7-\\\\U0001E8CF\\\\U0001F100-\\\\U0001F10C\\W\\d_]+";

    private final static String BLANK = "";

    public static String replaceAll(String source) {
        return replaceAll(source, REGREX, BLANK);
    }

    private static String replaceAll(String source, String regex, String replacement) {
        if (regex == null) {
            regex = REGREX;
        }
        if (replacement == null) {
            replacement = BLANK;
        }
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(source);
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        }
        return source;
    }
}
