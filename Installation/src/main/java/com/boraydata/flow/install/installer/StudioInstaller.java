package com.boraydata.flow.install.installer;

import com.boraydata.flow.install.common.ConfigTool;
import com.boraydata.flow.install.common.Writer;
import com.boraydata.flow.install.config.StudioApplicationConfig;
import com.boraydata.flow.install.util.Constant;

import java.io.IOException;
import java.util.Map;

public class StudioInstaller implements Installer {

    private final int serverPort;
    private final String driverClassName;
    private final String datasourceURL;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String databaseType;
    private final String logFile;
    private final String properties;
    private final String authServer;
    private String path;
    private String otherNodes;

    public StudioInstaller(int serverPort, String driverClassName, String datasourceURL, String datasourceUsername,
                           String datasourcePassword, String databaseType, String properties, String path, String authServer, String otherNodes) {
        this.serverPort = serverPort;
        this.driverClassName = driverClassName;
        this.datasourceURL = datasourceURL;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.databaseType = databaseType;
        this.logFile = path + "/logs";
        this.properties = properties;
        this.path = path;
        this.authServer = authServer;
        this.otherNodes = otherNodes;
    }

    @Override
    public void config() throws IOException {
        StudioApplicationConfig appConfig = new StudioApplicationConfig();
        appConfig.getServer().put(Constant.PORT, this.serverPort);
        appConfig.getServer().put(Constant.CONNECTION_TIMEOUT, Constant.CONNECTION_TIMEOUT_VALUE);
        Map<String, Object> servletValue = ConfigTool.config(Constant.SESSION, ConfigTool.config(Constant.TIMEOUT, Constant.TIMEOUT_VALUE));
        servletValue.put(Constant.CONTEXT_PATH, Constant.CONTEXT_PATH_VALUE);
        appConfig.getServer().put(Constant.SERVER_SERVLET, servletValue);

        Map<String, Object> encoding = ConfigTool.config(Constant.FORCE, Constant.FORCE_VALUE);
        ConfigTool.config(encoding, Constant.CHARSET, Constant.CHARSET_VALUE);
        ConfigTool.config(encoding, Constant.SPRING_HTTP_ENCODING_ENABLED, Constant.SPRING_HTTP_ENCODING_ENABLED_VALUE);
        appConfig.getSpring().put(Constant.HTTP, ConfigTool.config(Constant.ENCODING, encoding));

        appConfig.getSpring().put(Constant.MAIN, ConfigTool.config(Constant.ALLOW_BEAN_DEFINITION_OVERRIDING, Constant.ALLOW_BEAN_DEFINITION_OVERRIDING_VALUE));

        Map<String, Object> multipart = ConfigTool.config(Constant.MAX_FILE_SIZE, Constant.MAX_FILE_SIZE_VALUE);
        ConfigTool.config(multipart, Constant.MAX_REQUEST_SIZE, Constant.MAX_REQUEST_SIZE_VALUE);
        appConfig.getSpring().put(Constant.SPRING_SERVLET, ConfigTool.config(Constant.MULTIPART, multipart));

        Map<String, Object> datasource = ConfigTool.config(Constant.DRIVER_CLASS_NAME, this.driverClassName);
        Map<String, Object> dbcp2 = ConfigTool.config(Constant.VALIDATION_QUERY, Constant.VALIDATION_QUERY_VALUE);
        ConfigTool.config(dbcp2, Constant.TEST_ON_BORROW, Constant.TEST_ON_BORROW_VALUE);
        ConfigTool.config(datasource, Constant.DBCP2, dbcp2);
        ConfigTool.config(datasource, Constant.URL, this.datasourceURL);
        ConfigTool.config(datasource, Constant.USERNAME, this.datasourceUsername);
        ConfigTool.config(datasource, Constant.PASSWORD, this.datasourcePassword);
        appConfig.getSpring().put(Constant.DATASOURCE, datasource);

        Map<String, Object> jpa = ConfigTool.config(Constant.HIBERNATE, ConfigTool.config(Constant.DDL_AUTO, Constant.DDL_AUTO_VALUE));
        ConfigTool.config(jpa, Constant.SHOW_SQL, Constant.SHOW_SQL_VALUE);
        ConfigTool.config(jpa, Constant.DATABASE, this.databaseType);
        appConfig.getSpring().put(Constant.JPA, jpa);

        Map<String, Object> mvcIgnored = ConfigTool.config(Constant.GET, Constant.GET_VALUE);
        ConfigTool.config(mvcIgnored, Constant.POST, Constant.POST_VALUE);
        ConfigTool.config(mvcIgnored, Constant.ALL, Constant.ALL_VALUE);
        ConfigTool.config(mvcIgnored, Constant.REDIRECT_LOGIN_URL, Constant.REDIRECT_LOGIN_URL_VALUE);
        appConfig.getSecurity().put(Constant.MVC_IGNORED, mvcIgnored);
        appConfig.getSecurity().put(Constant.AUTH_SERVER, this.authServer);

        appConfig.getQuartz().put(Constant.QUARTZ_ENABLED, Constant.QUARTZ_ENABLED_VALUE);

        appConfig.getProperties().put(Constant.FILE, this.properties);
        appConfig.getProperties().put(Constant.OTHER_NODES, this.otherNodes);


        Writer.writeYaml(appConfig, path + "/cfg/" + Constant.STUDIO_APPLICATION_YAML_FILE);
    }
}