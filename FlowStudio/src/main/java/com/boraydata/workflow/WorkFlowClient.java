package com.boraydata.workflow;

import com.boraydata.common.CommonGlobal;
import com.boraydata.flowauth.constants.SymbolConstants;
import com.boraydata.flowauth.utils.ErrorUtils;
import com.boraydata.workflow.exceptions.ApplicationChangedException;
import com.boraydata.workflow.utils.ReplaceUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.boraydata.workflow.utils.DateUtils;
import com.boraydata.workflow.entity.JobState;
import com.boraydata.workflow.entity.State;
import com.boraydata.workflow.exceptions.DeployException;
import com.boraydata.workflow.exceptions.ShutdownException;
import com.boraydata.workflow.utils.ClientUtil;
import org.apache.flink.api.common.JobStatus;
import org.apache.flink.configuration.*;
import org.apache.flink.runtime.messages.Acknowledge;
import org.apache.flink.shaded.curator4.com.google.common.collect.Streams;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterRetrieveException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.application.ApplicationConfiguration;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.ClusterClientProvider;
import org.apache.flink.yarn.YarnClientYarnClusterInformationRetriever;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.YarnClusterInformationRetriever;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.HdfsConfiguration;
import org.apache.hadoop.io.file.tfile.TFile;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.cli.LogsCLI;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.apache.flink.configuration.MemorySize.MemoryUnit.MEGA_BYTES;

/**
 * Work Flow Client can manage the work flow lifecycle
 * Env variable FLOW_HOME must be set. It is linked to Flink Home
 */
public class WorkFlowClient implements AutoCloseable {
    private final static Logger log = LoggerFactory.getLogger(WorkFlowClient.class.getName());

    private final static String FLOW_DIR = "/" + CommonGlobal.PRODUCT_NAME();
    private final static String FLOW_APP_JAR = "FlowEngine-1.0.jar";
    private final static String FLOW_DIST_JAR = "flink-dist_2.12-1.13.1.jar";

    private String HDFSURL;
    private String FLOW_HOME;
    private String flowHdfs;
    private String baseUserRepo;
    private YarnClient yarnClient;
    private YarnConfiguration yarnConfiguration;
    private Configuration flinkConfiguration;
    private Properties cfgProps;
    private ObjectMapper mapper;

//    public static void main(String[] args) {
//        Properties cfgProps=new Properties();
//        cfgProps.setProperty("HdfsUrl", "hdfs://flow0:8020");
//        cfgProps.setProperty("FlowStore", "flow1:9092");
//        cfgProps.setProperty("DB.type", "mysql");
//        cfgProps.setProperty("DB.url", "jdbc:mysql://flow2:3306/fabricflow");
//        cfgProps.setProperty("DB.username", "flowv3");
//        cfgProps.setProperty("DB.password", "Data2fabric@flow");
//        WorkFlowClient client = new WorkFlowClient(cfgProps);
//        try {
//            String applicationId = client.deploy(1L, "WorkFlowTest");
//            System.out.println("applicationId="+applicationId);
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    /**
     * Return the user base repository
     *
     * @param HdfsUrl
     * @param accountID
     * @return
     */
    public static String getUserBaseRepository(String HdfsUrl, Long accountID) {
        return HdfsUrl + FLOW_DIR + "/users/" + accountID;
    }

    /**
     * Parse stream names from work flow config
     *
     * @param wf_content
     * @return
     */
    public static List<String> parseWorkflowStreams(String wf_content) {
        List<String> streams = new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            JsonNode jsonNode = mapper.readTree(wf_content);
            JsonNode sourceNode = jsonNode.at("/pipeline/sources");
            if (sourceNode.isArray()) {
                Streams.stream(sourceNode.elements()).forEach(node ->
                        streams.add(node.get("frn").asText().split(":")[1]));
            } else {
                String s_frn = sourceNode.get("frn").asText().split(":")[1];
                streams.add(s_frn);
            }
            JsonNode outputNode = jsonNode.at("/pipeline/output");
            if (outputNode.isArray()) {
                Streams.stream(outputNode.elements()).forEach(node ->
                        streams.add(node.get("frn").asText().split(":")[1]));
            } else {
                String s_frn = outputNode.get("frn").asText().split(":")[1];
                streams.add(s_frn);
            }
        } catch (Exception ex) {
            throw new DeployException(ex);
        }
        return streams;
    }


    /**
     * WorkFlowClient Construction
     *
     * @param cfgProps WorkFlow application config properties. included
     *                 flow store borkers, database connection properties, etc.
     *                 Property keys: HdfsUrl, FlowStore, DB.url, DB.username, DB.password,
     *                 DB.type, DB.poolsize
     */
    public WorkFlowClient(Properties cfgProps) {
        checkCfg(cfgProps);
        this.cfgProps = cfgProps;
        String FLOW_HOME = System.getenv("FLOW_HOME");
        if (FLOW_HOME == null || FLOW_HOME.trim().equals(""))
            throw new RuntimeException("Variable FLOW_HOME is undefined");
        this.FLOW_HOME = FLOW_HOME;
        this.HDFSURL = cfgProps.getProperty("HdfsUrl");
        flowHdfs = HDFSURL + FLOW_DIR;
        baseUserRepo = flowHdfs + "/users";
        init();
    }

    private void checkCfg(Properties cfgProps) {
        if (!cfgProps.containsKey("FlowStore") || !cfgProps.containsKey("DB.type") ||
                !cfgProps.containsKey("DB.url") || !cfgProps.containsKey("DB.username") ||
                !cfgProps.containsKey("DB.password") || !cfgProps.containsKey("HdfsUrl"))
            throw new RuntimeException("Missing workflow application configuration");
    }

    private void init() {
        yarnClient = YarnClient.createYarnClient();
        yarnConfiguration = new YarnConfiguration();
//        String HADOOP_CONF_DIR=System.getenv("HADOOP_CONF_DIR");
//        if ((HADOOP_CONF_DIR !=null)) {
//            yarnConfiguration.addResource(new Path(HADOOP_CONF_DIR + File.separatorChar + "core-site.xml"));
//            yarnConfiguration.addResource(new Path(HADOOP_CONF_DIR + File.separatorChar + "hdfs-site.xml"));
//            yarnConfiguration.addResource(new Path(HADOOP_CONF_DIR + File.separatorChar + "yarn-site.xml"));
//        }
        yarnClient.init(yarnConfiguration);
        yarnClient.start();

        String configurationDirectory = FLOW_HOME + "/conf";
        flinkConfiguration = GlobalConfiguration.loadConfiguration(
                configurationDirectory);
        flinkConfiguration.setString("$internal.deployment.config-dir", configurationDirectory);
        flinkConfiguration.setString("$internal.yarn.log-config-file", configurationDirectory + "/log4j.properties");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    private ApplicationId parse(String appID) {
        String[] tmp = appID.replaceAll("application_", "").split("_");
        return ApplicationId.newInstance(Long.parseLong(tmp[0]), Integer.parseInt(tmp[1]));
    }

    private String getDeployParams() throws IOException {
        StringWriter sw = new StringWriter();
        this.cfgProps.store(sw, "");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
            gzipOutputStream.write(sw.toString().getBytes(StandardCharsets.UTF_8));
            gzipOutputStream.close();
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    private String getDeployParams(String cfgParam) {
        byte[] decode = Base64.getDecoder().decode(cfgParam);
        StringBuilder readStr = new StringBuilder();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decode);
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             InputStreamReader reader = new InputStreamReader(gzipInputStream);
             BufferedReader in = new BufferedReader(reader)) {
            String readed;
            while ((readed = in.readLine()) != null) {
                readStr.append(readed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readStr.toString();
    }

    /**
     * Deploy WorkFlow Application
     *
     * @param wflowID   WorkFlow ID
     * @param accountID account id
     * @param flowName  WorkFlow Name
     * @param props     deploy work flow properties.
     *                  Supported properties: MasterMemSize, WorkerMemSize, WorkerSlots
     * @return Return workflow application id if success
     */
    public String deploy(Long wflowID, Long accountID, String flowName, Optional<Properties> props)
            throws IOException {
        String cfgParam = getDeployParams();
        String applicationName = CommonGlobal.PRODUCT_NAME() + "-" + flowName;
        String flinkLibs = flowHdfs + "/libs";
        String pluginLibs = flowHdfs + "/plugins";
        String userJarPath = flowHdfs + "/" + FLOW_APP_JAR;
        String flinkDistJar = flowHdfs + "/libs/" + FLOW_DIST_JAR;

        YarnClusterInformationRetriever clusterInformationRetriever = YarnClientYarnClusterInformationRetriever
                .create(yarnClient);
        flinkConfiguration.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, true);
        flinkConfiguration.set(PipelineOptions.JARS,
                Collections.singletonList(userJarPath));

        Path flinkLib = new Path(flinkLibs);
        String userLib = baseUserRepo + "/" + accountID + "/" + flowName + "/udfjars";
        Path workflowLib = new Path(userLib);
        Path pluginsLib = new Path(pluginLibs);
        List<String> libs = new ArrayList<>();
        libs.add(flinkLib.toString());
        libs.add(pluginsLib.toString());
        libs.add(workflowLib.toString());
        flinkConfiguration.set(YarnConfigOptions.PROVIDED_LIB_DIRS,
                Collections.unmodifiableList(libs));

        flinkConfiguration.set(YarnConfigOptions.FLINK_DIST_JAR, flinkDistJar);
        //设置为application模式
        flinkConfiguration.set(DeploymentOptions.TARGET, YarnDeploymentTarget.APPLICATION.getName());
        //yarn application name
        flinkConfiguration.set(YarnConfigOptions.APPLICATION_NAME, applicationName);

        //JobManager default memory size, 1G
        String jobManagerMemSize = "1024";
        //TaskManager default memory size, 1G
        String taskManagerMemSize = "1024";
        //default Task slots
        int taskSlots = 1;
        if (props.isPresent()) {
            if (props.get().containsKey("MasterMemSize"))
                jobManagerMemSize = props.get().getProperty("MasterMemSize");
            if (props.get().containsKey("WorkerMemSize"))
                taskManagerMemSize = props.get().getProperty("WorkerMemSize");
            if (props.get().containsKey("WorkerSlots"))
                taskSlots = Integer.parseInt(props.get().getProperty("WorkerSlots"));
        }
        flinkConfiguration.set(JobManagerOptions.TOTAL_PROCESS_MEMORY,
                MemorySize.parse(jobManagerMemSize, MEGA_BYTES));
        flinkConfiguration.set(TaskManagerOptions.TOTAL_PROCESS_MEMORY,
                MemorySize.parse(taskManagerMemSize, MEGA_BYTES));
        flinkConfiguration.set(TaskManagerOptions.NUM_TASK_SLOTS, taskSlots);

        //flinkConfiguration.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, true);

        ClusterSpecification clusterSpecification = new ClusterSpecification.ClusterSpecificationBuilder()
                .createClusterSpecification();

//		设置用户jar的参数和主类
        String[] args = {"--cfg", cfgParam, "--wf", wflowID.toString()};
        String appClassName = "com.boraydata.flowengine.FlowApplication";
        ApplicationConfiguration appConfig = new ApplicationConfiguration(args, appClassName);

        YarnClusterDescriptor yarnClusterDescriptor = new YarnClusterDescriptor(
                flinkConfiguration, yarnConfiguration, yarnClient,
                clusterInformationRetriever, true);
        ClusterClientProvider<ApplicationId> clusterClientProvider = null;
        try {
            clusterClientProvider = yarnClusterDescriptor.deployApplicationCluster(
                    clusterSpecification, appConfig);
        } catch (ClusterDeploymentException e) {
            throw new DeployException(e);
        }

        ClusterClient<ApplicationId> clusterClient = clusterClientProvider.getClusterClient();
        ApplicationId applicationId = clusterClient.getClusterId();

        return applicationId.toString();
    }

    /**
     * Shutdown work flow application
     *
     * @param appID
     */
    public void kill(String appID) {
        try {
            yarnClient.killApplication(parse(appID));
        } catch (YarnException e) {
            throw new ShutdownException(e);
        } catch (IOException e) {
            throw new ShutdownException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (yarnClient != null)
            yarnClient.close();
    }

    /**
     * Get flow application Status
     *
     * @param appID
     * @return Return workflow application status
     */
    public State getStatus(String appID) throws IOException, YarnException {
        State status;
        try {
            ApplicationReport applicationReport = yarnClient.getApplicationReport(parse(appID));
            status = new State();
            status.setYarnStartTime(DateUtils.toDate(applicationReport.getStartTime()));
            status.setYarnFinishTime(DateUtils.toDate(applicationReport.getFinishTime()));
            status.setYarnState(applicationReport.getYarnApplicationState().toString());
            status.setYarnFinalState(applicationReport.getFinalApplicationStatus().toString());
        } catch (org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException e) {
            throw new ApplicationChangedException(e);
        }

        try {
            ClusterClient<ApplicationId> clusterClient = getFlinkClusterClient(appID);
            List<JobState> jobStates = clusterClient.listJobs().get().stream().map(j -> {
                try {
                    return new JobState(j.getJobName(), clusterClient.getJobStatus(j.getJobId()).get().name());
                } catch (InterruptedException | ExecutionException e) {
                    return null;
                }
            }).collect(Collectors.toList());
            for (JobState s : jobStates) {
                if (s.getJobState() == null) {
                    status.setJobState(status.getYarnFinalState());
                    continue;
                }
                if (!s.getJobState().equals(JobStatus.RUNNING.name())) {
                    status.setJobState(s.getJobState());
                    break;
                }
                status.setJobState(JobStatus.RUNNING.name());
            }
            status.setJobStates(jobStates);
        } catch (Exception e) {
            status.setJobState(status.getYarnFinalState());
        } finally {
            if (status.getJobState() == null) {
                status.setJobState(status.getYarnState());
            }
        }

        return status;
    }

    /**
     * stop work flow application (stop all flink job)
     *
     * @param appID
     */
    public List<String> shutdown(String appID) throws Exception {
        ClusterClient<ApplicationId> clusterClient;
        try {
            clusterClient = getFlinkClusterClient(appID);
        } catch (ClusterRetrieveException e) {
            return Collections.singletonList(ErrorUtils.getErrorMessage(e));
        }
        return clusterClient.listJobs().get().stream().map(j -> {
            Acknowledge cancelAcknowledge = clusterClient.cancel(j.getJobId()).join();
            return cancelAcknowledge.toString();
        }).collect(Collectors.toList());
    }

    /**
     * get deployed workflow logs
     *
     * @param appID
     */
    public String getDeployedWorkFlowLog(String appID) throws IOException, YarnException {
        String logDir = yarnConfiguration.get(YarnConfiguration.NM_REMOTE_APP_LOG_DIR);
        log.info("local log: " + yarnConfiguration.get(YarnConfiguration.NM_LOG_DIRS));
        ApplicationReport applicationReport;
        try {
            applicationReport = yarnClient.getApplicationReport(parse(appID));
        } catch (org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException e) {
            throw new ApplicationChangedException(e);
        }
        String yarnLogPath = ClientUtil.hdfsUrl() + "/" + logDir + "/" + applicationReport.getUser() + "/" + "logs" + "/" + appID;
        Path path = new Path(yarnLogPath);
        org.apache.hadoop.conf.Configuration config = yarnClient.getConfig();
        FileSystem fileSystem = ClientUtil.fileSystem();
        RemoteIterator<LocatedFileStatus> logs = fileSystem.listFiles(path, false);
        StringBuilder logStr = new StringBuilder();
        while (logs.hasNext()) {
            LocatedFileStatus next = logs.next();
            String name = next.getPath().getName();
            FSDataInputStream open = fileSystem.open(next.getPath());
            TFile.Reader reader = new TFile.Reader(open, next.getLen(), config);
            TFile.Reader.Scanner scanner = reader.createScanner();
            byte[] bytes = new byte[0];
            while (!scanner.atEnd()) {
                TFile.Reader.Scanner.Entry e = scanner.entry();
                byte[] buf = new byte[e.getValueLength()];
                int len = e.getValue(buf);
                byte[] temp = new byte[len + bytes.length];
                System.arraycopy(bytes, 0, temp, 0, bytes.length);
                System.arraycopy(buf, 0, temp, bytes.length, len);
                bytes = temp;
                scanner.advance();
            }
            open.close();
            logStr.append(name).append(SymbolConstants.LINE_FEED).append(new String(bytes, StandardCharsets.UTF_8));
        }
        fileSystem.close();
        return Base64.getEncoder().encodeToString(logStr.toString().getBytes(StandardCharsets.UTF_8));
    }

//    public String getDeployedWorkFlowLogText(String appID) throws IOException, YarnException {
//        ApplicationReport applicationReport;
//        try {
//            applicationReport = yarnClient.getApplicationReport(parse(appID));
//        } catch (org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException e) {
//            throw new ApplicationChangedException(e);
//        }
//        LogCLIHelpers logCLIHelpers = new LogCLIHelpers();
//        logCLIHelpers.setConf(yarnConfiguration);
////        String fileName = "/tmp/" + appID + ".log";
////        PrintStream out = new PrintStream(fileName);
////        logCLIHelpers.dumpAllContainersLogs(parse(appID), applicationReport.getUser(), out);
////        if (applicationReport.getFinalApplicationStatus() == FinalApplicationStatus.UNDEFINED) {
////            logCLIHelpers.dumpAllContainersLogs(new ContainerLogsRequest(parse(appID), false, applicationReport.getUser(),
////                    null, null, null, null, null, 0L, null));
////        } else {
////            logCLIHelpers.dumpAllContainersLogs(new ContainerLogsRequest(parse(appID), true, applicationReport.getUser(),
////                    null, null, null, null, null, 0L, null));
////        }
//
//        LogsCLI logsCLI = new LogsCLI();
//        Set<String> matchedContainerLogFiles;
//        if (applicationReport.getFinalApplicationStatus() == FinalApplicationStatus.UNDEFINED) {
//            matchedContainerLogFiles = logsCLI.getMatchedContainerLogFiles(new ContainerLogsRequest(parse(appID), false, applicationReport.getUser(),
//                    null, null, null, null, null, 0L, null), false, true);
//        } else {
//            matchedContainerLogFiles = logsCLI.getMatchedContainerLogFiles(new ContainerLogsRequest(parse(appID), true, applicationReport.getUser(),
//                    null, null, null, null, null, 0L, null), false, true);
//        }
//        for (String matchedContainerLogFile : matchedContainerLogFiles) {
//            log.info(matchedContainerLogFile);
//        }
//
////        byte[] encoded = Files.readAllBytes(Paths.get(fileName));
//        return null;
//    }


//    public String getDeployedWorkFlowLogText(String appID) throws IOException, YarnException {
//        String logDir = yarnConfiguration.get(YarnConfiguration.NM_REMOTE_APP_LOG_DIR);
//        ApplicationReport applicationReport;
//        try {
//            applicationReport = yarnClient.getApplicationReport(parse(appID));
//        } catch (org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException e) {
//            throw new ApplicationChangedException(e);
//        }
//        String yarnLogPath = ClientUtil.hdfsUrl() + "/" + logDir + "/" + applicationReport.getUser() + "/" + "logs" + "/" + appID;
//        Path path = new Path(yarnLogPath);
//        org.apache.hadoop.conf.Configuration config = yarnClient.getConfig();
//        FileSystem fileSystem = ClientUtil.fileSystem();
//        RemoteIterator<LocatedFileStatus> logs = fileSystem.listFiles(path, false);
//        StringBuilder logStr = new StringBuilder();
//        while (logs.hasNext()) {
//            LocatedFileStatus next = logs.next();
//            String name = next.getPath().getName();
//            FSDataInputStream open = fileSystem.open(next.getPath());
//            TFile.Reader reader = new TFile.Reader(open, next.getLen(), config);
//            TFile.Reader.Scanner scanner = reader.createScanner();
//            byte[] bytes = new byte[0];
//            while (!scanner.atEnd()) {
//                TFile.Reader.Scanner.Entry e = scanner.entry();
//                if (!e.isValueLengthKnown()) {
//                    return null;
//                }
//                byte[] buf = new byte[e.getValueLength()];
//                int len = e.getValue(buf);
//                byte[] temp = new byte[len + bytes.length];
//                System.arraycopy(bytes, 0, temp, 0, bytes.length);
//                System.arraycopy(buf, 0, temp, bytes.length, len);
//                bytes = temp;
//                scanner.advance();
//            }
//            open.close();
//            logStr.append(name).append(SymbolConstants.LINE_FEED).append(new String(bytes, StandardCharsets.UTF_8));
//        }
//        fileSystem.close();
//        return logStr.toString();
//    }

    public String getDeployedWorkFlowLogText(String appID) throws IOException, YarnException {

        log.info("log dir:" + yarnConfiguration.get(YarnConfiguration.NM_LOG_DIRS));
        log.info("yarn.log.dir: " + System.getProperty("yarn.log.dir"));
        ApplicationReport applicationReport;
        try {
            List<ApplicationAttemptReport> applicationAttempts = yarnClient.getApplicationAttempts(parse(appID));
            applicationAttempts.forEach(a -> {
                
            });
            applicationReport = yarnClient.getApplicationReport(parse(appID));
        } catch (org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException e) {
            throw new ApplicationChangedException(e);
        }

        if (applicationReport.getFinalApplicationStatus() != FinalApplicationStatus.UNDEFINED) {
            log.info("finish");
            String logDir = yarnConfiguration.get(YarnConfiguration.NM_REMOTE_APP_LOG_DIR);
            String yarnLogPath = ClientUtil.hdfsUrl() + "/" + logDir + "/" + applicationReport.getUser() + "/" + "logs" + "/" + appID;
            Path path = new Path(yarnLogPath);
            org.apache.hadoop.conf.Configuration config = yarnClient.getConfig();
            FileSystem fileSystem = ClientUtil.fileSystem();
            RemoteIterator<LocatedFileStatus> logs = fileSystem.listFiles(path, false);
            StringBuilder logStr = new StringBuilder();
//            while (logs.hasNext()) {
//                LocatedFileStatus next = logs.next();
//                String name = next.getPath().getName();
//                FSDataInputStream open = fileSystem.open(next.getPath());
//                TFile.Reader reader = new TFile.Reader(open, next.getLen(), config);
//                TFile.Reader.Scanner scanner = reader.createScanner();
//                logStr.append(name).append(SymbolConstants.LINE_FEED);
//                while (!scanner.atEnd()) {
//                    TFile.Reader.Scanner.Entry e = scanner.entry();
//                    byte[] buf = new byte[e.getValueLength()];
//                    e.getValue(buf);
//                    String str = new String(buf, StandardCharsets.UTF_8);
//                    logStr.append(str).append(SymbolConstants.LINE_FEED);
//                    scanner.advance();
//                }
//                open.close();
//            }
//
//            fileSystem.close();
            while (logs.hasNext()) {
                LocatedFileStatus next = logs.next();
                String name = next.getPath().getName();
                FSDataInputStream open = fileSystem.open(next.getPath());
                TFile.Reader reader = new TFile.Reader(open, next.getLen(), config);
                TFile.Reader.Scanner scanner = reader.createScanner();
                byte[] bytes = new byte[0];
                while (!scanner.atEnd()) {
                    TFile.Reader.Scanner.Entry e = scanner.entry();
                    if (!e.isValueLengthKnown()) {
                        return null;
                    }
                    byte[] buf = new byte[e.getValueLength()];
                    int len = e.getValue(buf);
                    byte[] temp = new byte[len + bytes.length];
                    System.arraycopy(bytes, 0, temp, 0, bytes.length);
                    System.arraycopy(buf, 0, temp, bytes.length, len);
                    bytes = temp;
                    scanner.advance();
                }
                open.close();
                logStr.append(name).append(SymbolConstants.LINE_FEED).append(new String(bytes, StandardCharsets.UTF_8));
            }
            fileSystem.close();
            return logStr.toString();
        } else {
            log.info("running");

            String logDir = yarnConfiguration.get(YarnConfiguration.NM_LOG_DIRS);
            String replace = logDir.replace("${yarn.log.dir}", "/opt/hadoop/logs");
            String yarnLogPath = replace + "/" + appID; //+ "/" + "jobmanager.log";
            File file = new File(yarnLogPath);
            StringBuilder logStr = new StringBuilder();
            File[] files = file.listFiles();
            if (files == null)
                return logStr.toString();
            for (File f : files) {
                if (f.isDirectory()) {
                    File[] containerFiles = f.listFiles();
                    if (containerFiles == null)
                        continue;
                    for (File containerFile : containerFiles) {
                        if (containerFile.getName().contains(".log")) {
                            byte[] bytes = Files.readAllBytes(Paths.get(containerFile.getAbsolutePath()));
                            logStr.append(f.getName()).append(SymbolConstants.LINE_FEED);
                            logStr.append(new String(bytes, StandardCharsets.UTF_8));
                        }
                    }
                } else {
                    if (f.getName().contains(".log")) {
                        byte[] bytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                }
            }
            return logStr.toString();
        }
    }

    private ClusterClient<ApplicationId> getFlinkClusterClient(String appID) throws ClusterRetrieveException {
        YarnClusterInformationRetriever clusterInformationRetriever = YarnClientYarnClusterInformationRetriever
                .create(yarnClient);
        YarnClusterDescriptor yarnClusterDescriptor = new YarnClusterDescriptor(
                flinkConfiguration, yarnConfiguration, yarnClient,
                clusterInformationRetriever, true);
        ClusterClientProvider<ApplicationId> retrieve = yarnClusterDescriptor.retrieve(parse(appID));
        return retrieve.getClusterClient();
    }
}
