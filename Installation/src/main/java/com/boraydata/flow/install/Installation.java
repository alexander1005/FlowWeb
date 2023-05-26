package com.boraydata.flow.install;

import com.beust.jcommander.JCommander;
import com.boraydata.flow.install.common.DatasourceFactory;
import com.boraydata.flow.install.util.ApplicationType;
import com.boraydata.flow.install.util.Options;
import com.boraydata.flow.install.installer.Installer;
import com.boraydata.flow.install.installer.RegistryInstaller;
import com.boraydata.flow.install.installer.StudioInstaller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.boraydata.flow.install.util.Propertie.*;

public class Installation {

    private static final Logger logger = LoggerFactory.getLogger(Installation.class);

    public static void main(String[] args) {
        Options ops = new Options();
        JCommander cmd = new JCommander(ops, null, args);

        if (ops.isNull()) {
            logger.error("options can not be empty");
            System.exit(1);
        }

        if (ops.help) {
            cmd.usage();
            System.exit(1);
        }

        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream(ops.propertiesFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Failed to load the properties file");
            System.exit(1);
        }

        String propertiesFile = ops.path + "/cfg/flowstudio-server.properties";

        try {

            ApplicationType applicationType = ApplicationType.valueOf(ops.application);
            Installer installer;
            switch (applicationType) {
                case registry:
                    String sessionTimeout = properties.getProperty(expire) == null ? "14400" : properties.getProperty(expire);
                    String idleTimeout = properties.getProperty(idle) == null ? "1800" : properties.getProperty(idle);
                    installer = new RegistryInstaller(Integer.parseInt(properties.getProperty(serverPort)),
                            getDatabaseDriverClass(properties.getProperty(databaseType)),
                            properties.getProperty(datasourceURL),
                            properties.getProperty(datasourceUsername),
                            properties.getProperty(datasourcePassword),
                            properties.getProperty(databaseType),
                            properties.getProperty(flowStore),
                            properties.getProperty(authServer),
                            ops.path,
                            Long.parseLong(sessionTimeout) * 1000,
                            Long.parseLong(idleTimeout) * 1000,
                            properties.getProperty(registryNodes),
                            properties.getProperty(zookeeperHost),
                            properties.getProperty(promethuesHost),
                            properties.getProperty(intervalsMillisecond)
                    );
                    break;
                case studio:
                    properties(propertiesFile,
                            properties.getProperty(hdfsUrl),
                            properties.getProperty(flowStore),
                            properties.getProperty(datasourceURL),
                            properties.getProperty(datasourceUsername),
                            properties.getProperty(datasourcePassword),
                            properties.getProperty(databaseType),
                            properties.getProperty(dbPoolSize),
                            properties.getProperty(masterMemSize),
                            properties.getProperty(workerMemSize),
                            properties.getProperty(workerSlots));
                    installer = new StudioInstaller(Integer.parseInt(properties.getProperty(serverPort)),
                            getDatabaseDriverClass(properties.getProperty(databaseType)),
                            properties.getProperty(datasourceURL),
                            properties.getProperty(datasourceUsername),
                            properties.getProperty(datasourcePassword),
                            properties.getProperty(databaseType),
                            propertiesFile,
                            ops.path,
                            properties.getProperty(authServer),
                            properties.getProperty(otherNodes)
                    );
                    break;
                default:
                    throw new IllegalStateException("Unexpected application type: " + applicationType);
            }

            installer.config();

            logger.info("Initialization succeeded");
        } catch (Exception e) {
            logger.error("ERROR", e);
            System.exit(1);
        }
    }

    private static void properties(String propertiesFile, String hdfsUrl, String flowStore, String dbUrl, String username,
                                   String dbPassword, String dbType, String poolSize, String masterMemSize, String workerMemSize,
                                   String workerSlots) throws IOException {
        Properties flowProperties = new Properties();
        flowProperties.setProperty(hdfsUrlConfig, hdfsUrl);
        flowProperties.setProperty(flowStoreConfig, flowStore);
        flowProperties.setProperty(dbUrlConfig, dbUrl);
        flowProperties.setProperty(dbUsernameConfig, username);
        flowProperties.setProperty(dbPasswordConfig, dbPassword);
        flowProperties.setProperty(dbTypeConfig, dbType);
        if (poolSize != null)
            flowProperties.setProperty(dbPoolSizeConfig, poolSize);
        if (masterMemSize != null)
            flowProperties.setProperty(masterMemSizeConfig, masterMemSize);
        if (workerMemSize != null)
            flowProperties.setProperty(workerMemSizeConfig, workerMemSize);
        if (workerSlots != null)
            flowProperties.setProperty(workerSlotsConfig, workerSlots);
        try (FileOutputStream outputStream = new FileOutputStream(propertiesFile, false)) {
            flowProperties.store(outputStream, null);
        }
    }

    private static String getDatabaseDriverClass(String dbType) {
        return DatasourceFactory.dscPropers.getProperty(dbType.toUpperCase());
    }
}

