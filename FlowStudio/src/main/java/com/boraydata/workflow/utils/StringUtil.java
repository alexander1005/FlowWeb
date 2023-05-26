package com.boraydata.workflow.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    public static List<String> readLineByLineToList(String filePath) {
        try (
                Stream<String> lines = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
        ) {
//            stream.forEach(s -> {
//                contentBuilder.add(s);
//                if (!String.valueOf(s).trim().isEmpty()) {
//                    contentBuilder.append("\n");
//                }
//            });
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readLineByLineToString(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (
                Stream stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8);
        ) {
            stream.forEach(s -> {
                contentBuilder.append(s);
                if (!String.valueOf(s).trim().isEmpty()) {
                    contentBuilder.append("\n");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }
}
