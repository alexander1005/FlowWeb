package com.boraydata.workflow.utils;

import com.boraydata.flowauth.constants.SymbolConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


@Component
public class PropertiesUtils {

    public static Properties PROPERTIES;
    public static String[] NODES;

    @Value("${properties.file}")
    public void setProperties(String file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        PROPERTIES = properties;
    }

    @Value("${properties.other-nodes}")
    public void setNodes(String nodes) {
        NODES = nodes.split(SymbolConstants.COMMA);
    }
}
