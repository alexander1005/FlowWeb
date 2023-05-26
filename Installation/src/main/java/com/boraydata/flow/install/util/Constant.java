package com.boraydata.flow.install.util;

public class Constant {
    public static final String PORT = "port"; // user
    public static final String CONNECTION_TIMEOUT = "connection-timeout";
    public static final long CONNECTION_TIMEOUT_VALUE = 18000000L;
    public static final String SERVER_SERVLET = "servlet";
    public static final String SESSION = "session";
    public static final String TIMEOUT = "timeout";
    public static final int TIMEOUT_VALUE = 3600;
    public static final String IDLE = "idle";
    public static final String CONTEXT_PATH = "context-path";
    public static final String CONTEXT_PATH_VALUE = "/api";

    public static final String HTTP = "http";
    public static final String ENCODING = "encoding";
    public static final String FORCE = "force";
    public static final boolean FORCE_VALUE = true;
    public static final String CHARSET = "charset";
    public static final String CHARSET_VALUE = "UTF-8";
    public static final String SPRING_HTTP_ENCODING_ENABLED = "enabled";
    public static final boolean SPRING_HTTP_ENCODING_ENABLED_VALUE = true;

    public static final String MAIN = "main";
    public static final String ALLOW_BEAN_DEFINITION_OVERRIDING = "allow-bean-definition-overriding";
    public static final boolean ALLOW_BEAN_DEFINITION_OVERRIDING_VALUE = true;

    public static final String SPRING_SERVLET = "servlet";
    public static final String MULTIPART = "multipart";
    public static final String MAX_FILE_SIZE = "max-file-size";
    public static final long MAX_FILE_SIZE_VALUE = 20971520000L;
    public static final String MAX_REQUEST_SIZE = "max-request-size";
    public static final long MAX_REQUEST_SIZE_VALUE = 209715200L;

    public static final String DATASOURCE = "datasource"; // user
    public static final String DRIVER_CLASS_NAME = "driver-class-name"; // user
    public static final String DBCP2 = "dbcp2";
    public static final String VALIDATION_QUERY = "validation-query";
    public static final String VALIDATION_QUERY_VALUE = "select 1";
    public static final String TEST_ON_BORROW = "test-on-borrow";
    public static final boolean TEST_ON_BORROW_VALUE = true;
    public static final String URL = "url"; // user
    public static final String USERNAME = "username"; // user
    public static final String PASSWORD = "password"; // user

    public static final String JPA = "jpa";
    public static final String HIBERNATE = "hibernate";
    public static final String DDL_AUTO = "ddl-auto";
    public static final String DDL_AUTO_VALUE = "none";
    public static final String SHOW_SQL = "show-sql";
    public static final boolean SHOW_SQL_VALUE = true;
    public static final String DATABASE = "database"; // user

    public static final String KAFKA = "kafka";
    public static final String CONSUMER = "consumer";
    public static final String GROUP_ID = "group-id";
    public static final String AUTO_OFFSET_RESET = "auto-offset-reset";
    public static final String AUTO_OFFSET_RESET_VALUE = "earliest";
    public static final String KEY_DESERIALIZER = "key-deserializer";
    public static final String KEY_DESERIALIZER_VALUE = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String VALUE_DESERIALIZER = "value-deserializer";
    public static final String VALUE_DESERIALIZER_VALUE = "org.apache.kafka.common.serialization.StringDeserializer";
    public static final String PRODUCER = "producer";
    public static final String KEY_SERIALIZER = "key-serializer";
    public static final String KEY_SERIALIZER_VALUE = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String VALUE_SERIALIZER = "value-serializer";
    public static final String VALUE_SERIALIZER_VALUE = "org.apache.kafka.common.serialization.StringSerializer";
    public static final String BOOTSTRAP_SERVERS = "bootstrap-servers"; // user

    public static final String MVC_IGNORED = "mvc-ignored";
    public static final String GET = "get";
    public static final String GET_VALUE = "/sms/send/**,/file/**";

    public static final String POST = "post";
    public static final String POST_VALUE = "/";

    public static final String ALL = "all";
    public static final String ALL_VALUE = "/login,/login/registry,/login/authentication,/v2/api-docs," +
            "/configuration/ui,/swagger-resources,/configuration/security,/swagger-ui.html,/webjars/**," +
            "/swagger-resources/configuration/ui," +
            "/static,/static/**,/index.html,/index.html/**,/api/**";

    public static final String REDIRECT_LOGIN_URL = "redirect-login-url";
    public static final String REDIRECT_LOGIN_URL_VALUE = "/login";

    public static final String AUTH_SERVER = "auth-server";

    public static final String AUTH = "auth";
    public static final String EXPIRE = "expire";

    public static final String QUARTZ_ENABLED = "enabled";
    public static final boolean QUARTZ_ENABLED_VALUE = true;

    public static final String NODES = "nodes";

    public static final String FILE = "file";
    public static final String OTHER_NODES = "other-nodes";

    public static final String REGISTRY_APPLICATION_YAML_FILE = "flowregistry.yml";
    public static final String STUDIO_APPLICATION_YAML_FILE = "flowstudio.yml";


    //监控相关的配置
    public static final String MONITOR = "monitor";
    public static final String ZOOKEEPERHOST = "zookeeper-host";
    public static final String PROMETHUESHOST = "promethues-host";
    public static final String EPHEMERALNAME = "ephemeral-name";
    public static final String EPHEMERALNAME_VALUE = "/flowRegistryLeadership1647489040";
    public static final String INTERVALSMILLISECOND = "intervals-millisecond";
    public static final String PROMETHUESSCRAPEPOOL = "promethues-scrapePool";
    public static final String PROMETHUESSCRAPEPOOL_VALUE = "zk_sd";
    public static final String PROMETHUESTARGES = "promethues-targes";
    public static final String PROMETHUESTARGES_VALUE = "api/v1/targets";

}
