package com.boraydata.flow.install.util;

import com.beust.jcommander.Parameter;

public class Options {

    @Parameter(names = {"--properties-file", "-p"}, description = "properties file for agent")
    public String propertiesFile;

    @Parameter(names = {"--app", "-a"}, description = "application type")
    public String application;

    @Parameter(names = {"--path"}, description = "application type")
    public String path;

    @Parameter(names = {"--help", "-h"}, help = true)
    public Boolean help = false;

    public boolean isNull() {
        return propertiesFile == null || application == null;
    }
}
