package com.boraydata.workflow.utils;

import com.boraydata.common.FlowConstants;
import com.boraydata.common.utils.WorkFlowConfigChecker;
import com.boraydata.flowauth.constants.SymbolConstants;
import com.boraydata.workflow.WorkFlowClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ClientUtil {

    public static String hdfsUrl() {
        return PropertiesUtils.PROPERTIES.getProperty("HdfsUrl");
    }

    private static String hdfsNameService() {
        return PropertiesUtils.PROPERTIES.getProperty("hdfs.nameservice");
    }

    private static String hdfsNameNodes() {
        return PropertiesUtils.PROPERTIES.getProperty("hdfs.namenodes");
    }

    public static String[] studioNodes() {
        return PropertiesUtils.NODES;
    }

    public static WorkFlowClient client() {
        return WorkFlowClientInst.INSTANCE.getInstance();
    }

    public static FileSystem fileSystem() throws IOException {
        if (hdfsNameNodes() != null && hdfsNameNodes().split(SymbolConstants.COMMA).length > 1) {
            return fileSystemHA(hdfsNameService(), hdfsNameNodes());
        }
        return FileSystem.get(URI.create(hdfsUrl()), new Configuration());
    }

    public static FileSystem fileSystemHA(String nameservice, String namenodes) throws IOException {
        Configuration hadoopConfig = new Configuration();
        hadoopConfig.set("fs.defaultFS", hdfsUrl());
        hadoopConfig.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
        hadoopConfig.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
        hadoopConfig.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true");
        hadoopConfig.set("ipc.client.fallback-to-simple-auth-allowed", "true");
        hadoopConfig.set("dfs.client.socket-timeout", "10000");

        String[] namenodesArray = namenodes.split(",");
        List<String> nnNames = IntStream.range(0, namenodesArray.length).mapToObj(i -> "nn" + (i + 1)).collect(Collectors.toList());
        hadoopConfig.set("dfs.nameservices", nameservice);
        hadoopConfig.set("dfs.ha.namenodes." + nameservice, nnNames.stream().collect(Collectors.joining(",")));
        for (int i = 0; i < nnNames.size(); i++) {
            hadoopConfig.set("dfs.namenode.rpc-address." + nameservice + "." + nnNames.get(i), namenodesArray[i].trim());
        }
        hadoopConfig.set("dfs.client.failover.proxy.provider." + nameservice,
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        return FileSystem.get(hadoopConfig);
    }

    public static String repository(String bk, String workflowName) {
        return bk + "/" + workflowName;
    }

    public static WorkFlowConfigChecker workFlowConfigChecker(DataSource dataSource) {
        String dbType = PropertiesUtils.PROPERTIES.getProperty("DB.type");

        return WorkFlowConfigChecker.getInstance(FlowConstants.DBTYPE.valueOf(dbType.toUpperCase()), dataSource);
    }
}
