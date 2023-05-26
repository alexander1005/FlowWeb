package com.boraydata.flow.install.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.*;
import java.util.Scanner;


public class Writer {

    public static void writeYaml(Object appConfig, String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        File file = new File(filename);
        objectMapper.writeValue(file, appConfig);

        Scanner fileScanner = new Scanner(file);
        fileScanner.nextLine();
        FileWriter fileStream = new FileWriter(file);
        BufferedWriter out = new BufferedWriter(fileStream);
        while(fileScanner.hasNextLine()) {
            String next = fileScanner.nextLine();
            next = next.replace("\"", "");
            if (next.contains("select 1"))
                next = next.replace("select 1", "'select 1'");
            if(next.equals("\n"))
                out.newLine();
            else
                out.write(next);
            out.newLine();
        }
        out.close();
    }
}
