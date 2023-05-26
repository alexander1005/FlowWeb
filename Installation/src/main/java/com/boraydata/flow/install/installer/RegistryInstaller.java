package com.boraydata.flow.install.installer;

import com.boraydata.flow.install.common.ConfigTool;
import com.boraydata.flow.install.common.Writer;
import com.boraydata.flow.install.config.RegistryApplicationConfig;
import com.boraydata.flow.install.util.Constant;
import com.boraydata.flow.install.config.AuthApplicationConfig;

import java.io.IOException;
import java.util.Map;

public class RegistryInstaller implements Installer {

    private final int serverPort;
    private final String driverClassName;
    private final String datasourceURL;
    private final String datasourceUsername;
    private final String datasourcePassword;
    private final String databaseType;
    private final String logFile;
    private final String kafkaGroupId;
    private final String bootstrapServers;
    private final String authServer;
    private final String path;
    private final long expire;
    private final long idle;
    private final String registryNodes;

    //监控相关的配置
    private String zookeeperHost;
    private String promethuesHost;
    private String intervalsMillisecond;

    public RegistryInstaller(int serverPort, String driverClassName, String datasourceURL, String datasourceUsername,
                             String datasourcePassword, String databaseType, String bootstrapServers,
                             String authServer, String path, long expire, long idle, String registryNodes,
                             String zookeeperHost, String promethuesHost, String intervalsMillisecond) {
        this.serverPort = serverPort;
        this.driverClassName = driverClassName;
        this.datasourceURL = datasourceURL;
        this.datasourceUsername = datasourceUsername;
        this.datasourcePassword = datasourcePassword;
        this.databaseType = databaseType;
        this.logFile = path + "/logs";
        this.kafkaGroupId = "flow_registry_id";
        this.bootstrapServers = bootstrapServers;
        this.authServer = authServer;
        this.path = path;
        this.expire = expire;
        this.idle = idle;
        this.registryNodes = registryNodes;
        //监控相关的配置
        this.zookeeperHost = zookeeperHost;
        this.promethuesHost = promethuesHost;
        this.intervalsMillisecond = intervalsMillisecond;
    }

    @Override
    public void config() throws IOException {
        RegistryApplicationConfig appConfig = new RegistryApplicationConfig();
        appConfig.getServer().put(Constant.PORT, this.serverPort);
        appConfig.getServer().put(Constant.CONNECTION_TIMEOUT, Constant.CONNECTION_TIMEOUT_VALUE);
        Map<String, Object> servletValue = ConfigTool.config(Constant.SESSION, ConfigTool.config(Constant.TIMEOUT, Constant.TIMEOUT_VALUE));
        servletValue.put(Constant.CONTEXT_PATH, Constant.CONTEXT_PATH_VALUE);
        appConfig.getServer().put(Constant.SERVER_SERVLET, servletValue);
        appConfig.getServer().put(Constant.IDLE, ConfigTool.config(Constant.TIMEOUT, idle));

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

        Map<String, Object> consumer = ConfigTool.config(Constant.GROUP_ID, this.kafkaGroupId);
        ConfigTool.config(consumer, Constant.AUTO_OFFSET_RESET, Constant.AUTO_OFFSET_RESET_VALUE);
        ConfigTool.config(consumer, Constant.KEY_DESERIALIZER, Constant.KEY_DESERIALIZER_VALUE);
        ConfigTool.config(consumer, Constant.VALUE_DESERIALIZER, Constant.VALUE_DESERIALIZER_VALUE);

        Map<String, Object> kafka = ConfigTool.config(Constant.CONSUMER, consumer);
        Map<String, Object> producer = ConfigTool.config(Constant.KEY_SERIALIZER, Constant.KEY_SERIALIZER_VALUE);
        ConfigTool.config(producer, Constant.VALUE_SERIALIZER, Constant.VALUE_SERIALIZER_VALUE);
        ConfigTool.config(kafka, Constant.PRODUCER, producer);
        ConfigTool.config(kafka, Constant.BOOTSTRAP_SERVERS, this.bootstrapServers);
        appConfig.getSpring().put(Constant.KAFKA, kafka);

        Map<String, Object> mvcIgnored = ConfigTool.config(Constant.GET, Constant.GET_VALUE);
        ConfigTool.config(mvcIgnored, Constant.POST, Constant.POST_VALUE);
        ConfigTool.config(mvcIgnored, Constant.ALL, Constant.ALL_VALUE);
        ConfigTool.config(mvcIgnored, Constant.REDIRECT_LOGIN_URL, Constant.REDIRECT_LOGIN_URL_VALUE);
        appConfig.getSecurity().put(Constant.MVC_IGNORED, mvcIgnored);
        appConfig.getSecurity().put(Constant.AUTH, ConfigTool.config(Constant.EXPIRE, expire));

        appConfig.getQuartz().put(Constant.QUARTZ_ENABLED, Constant.QUARTZ_ENABLED_VALUE);
        appConfig.getRegistry().put(Constant.NODES, registryNodes);
        //监控相关的配置
        appConfig.getMonitor().put(Constant.ZOOKEEPERHOST, this.zookeeperHost);
        appConfig.getMonitor().put(Constant.PROMETHUESHOST, this.promethuesHost);
        appConfig.getMonitor().put(Constant.EPHEMERALNAME, Constant.EPHEMERALNAME_VALUE);
        appConfig.getMonitor().put(Constant.INTERVALSMILLISECOND, this.intervalsMillisecond);
        appConfig.getMonitor().put(Constant.PROMETHUESTARGES, Constant.PROMETHUESTARGES_VALUE);
        appConfig.getMonitor().put(Constant.PROMETHUESSCRAPEPOOL, Constant.PROMETHUESSCRAPEPOOL_VALUE);

        Writer.writeYaml(appConfig, path + "/cfg/" + Constant.REGISTRY_APPLICATION_YAML_FILE);
    }
}
