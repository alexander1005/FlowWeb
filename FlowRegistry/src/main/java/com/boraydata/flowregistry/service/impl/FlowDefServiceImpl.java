package com.boraydata.flowregistry.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.boraydata.common.FlowConstants;
import com.boraydata.flowauth.constants.SymbolConstants;
import com.boraydata.flowauth.enums.FlowDefStatus;
import com.boraydata.flowauth.utils.ErrorUtils;
import com.boraydata.flowauth.utils.ValidateUtils;
import com.boraydata.flowregistry.dao.*;
import com.boraydata.flowregistry.entity.*;
import com.boraydata.flowregistry.entity.vo.*;
import com.boraydata.flowregistry.utils.UserUtils;
import com.boraydata.flowregistry.common.SchemaUtil;
import com.boraydata.flowregistry.entity.dto.*;
import com.boraydata.flowregistry.service.FlowDefService;
import com.boraydata.flowregistry.utils.SchemaType;
import com.google.common.collect.Iterables;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.boraydata.flowregistry.utils.SchemaType.*;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Service
public class FlowDefServiceImpl implements FlowDefService {

    private static final Logger logger = LoggerFactory.getLogger(FlowDefServiceImpl.class);

    @Autowired
    private FlowDefDAO flowDefDAO;

    @Autowired
    private ConfigDAO configDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private FlowTagsMappingDAO flowTagsMappingDAO;

    @Autowired
    private WorkFlowMappingDAO workFlowMappingDAO;

    @Autowired
    private FlowTagDAO flowTagDAO;

    @Autowired
    private WorkFlowDAO workFlowDAO;

    @Autowired
    private KafkaAdmin admin;

    @Autowired
    private SchemaUtil schemaUtil;

    @Override
    public FlowDef findByStreamName(String streamName) {
        return flowDefDAO.findByStreamName(streamName);
    }

    @Override
    public Iterable<FlowDef> findAll() {
        return flowDefDAO.findAll();
    }

    private ReentrantLock lock = new ReentrantLock();


    /**
     * update a stream shardCount field
     */
    @Override
    public FlowDefVO updateShardCount(FlowDefShardsDTO flowDefShardsDTO) {
        FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(flowDefShardsDTO.getStreamName(), UserUtils.currentUser().getId());
        if (flowDef == null)
            return flowDefToVOFailure("`streamName` is null");
        flowDef.setShards(flowDefShardsDTO.getShards());
        flowDef.setUpdateTime(new Date(System.currentTimeMillis()));

        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            kafkaAlterPartition(adminClient, flowDef.getStreamName(), flowDef.getShards());
            flowDefDAO.save(flowDef);
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
        return flowDefToVOSuccess(flowDef);
    }

    /**
     * update a stream retention field
     */
    @Override
    public FlowDefVO updateRetention(FlowDefRetentionDTO flowDefRetentionDTO) {
        FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(flowDefRetentionDTO.getStreamName(), UserUtils.currentUser().getId());
        if (flowDef == null)
            return flowDefToVOFailure("`streamName` is not exist");
        flowDef.setRetention(flowDefRetentionDTO.getRetention());
        flowDef.setUpdateTime(new Date(System.currentTimeMillis()));

        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            kafkaAlterRetention(adminClient, flowDef.getStreamName(), flowDef.getRetention());
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }

        return flowDefToVOSuccess(flowDefDAO.save(flowDef));
    }

    @Override
    public FlowDefVO updateStatus(FlowDefStatusDTO flowDefStatusDTO) {
        FlowDef save = null;
        try {
            FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(flowDefStatusDTO.getStreamName(), UserUtils.currentUser().getId());
            Integer status = flowDefStatusDTO.getStatus();
            FlowDefStatus flowDefStatus = FlowDefStatus.valueOf(status);
            if (flowDefStatus == null)
                return flowDefToVOFailure("`status` can only be passed in 1-4");
            flowDef.setStatus(status);
            flowDef.setUpdateTime(new Date(System.currentTimeMillis()));
            save = flowDefDAO.save(flowDef);
            return flowDefToVOSuccess(save);
        } catch (Throwable e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
    }

    /**
     * delete a stream & delete a kafka topic
     */
    @Override
    public FlowDefVO deleteByStreamName(String streamName) {
        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(streamName, UserUtils.currentUser().getId());
            if (flowDef == null)
                return flowDefToVOFailure("`" + streamName + "` stream is not exist");
            if (flowDef.getStatus() != null && FlowDefStatus.ACTIVE.status == flowDef.getStatus())
                return flowDefToVOFailure("`" + streamName + "` stream is active");
            flowDefDAO.deleteByStreamNameAndAccountId(streamName, UserUtils.currentUser().getId());
            if (flowDef.getStreamType() != 2)
                kafkaDeleteTopic(adminClient, Collections.singletonList(streamName));
            return null;
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
    }

    /**
     * delete all stream & delete all streams kafka topic
     */
    @Override
    public FlowDefVO deleteAll() {
        Long accountID = UserUtils.currentUser().getId();
        try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
            Iterable<FlowDef> addFlows = flowDefDAO.findAllByAccountId(accountID);
            List<String> collect = StreamSupport.stream(addFlows.spliterator(), false)
                    .map(f -> {
                        if (FlowDefStatus.ACTIVE.status == f.getStatus())
                            return null;
                        return f.getStreamName();
                    }).filter(Objects::nonNull).collect(Collectors.toList());
            if (collect.size() != Iterables.size(addFlows)) {
                return flowDefToVOFailure("some stream is avtive");
            }
            flowDefDAO.deleteAllByAccountId(accountID);
            kafkaDeleteTopic(adminClient, collect);
            return null;
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
    }

    @Override
    public ApplyCertsVO applyCerts(ApplyCertsDTO applyCertsDTO) {
        lock.lock();
        try {
            if (UserUtils.currentUser().getUsername().equals("admin")) {
                return new ApplyCertsVO(null, "`admin` user is invalid.");
            }
            Optional<User> userOptional = userDAO.findById(UserUtils.currentUser().getId());
            if (!userOptional.isPresent())
                return new ApplyCertsVO(null, "user is not present.");
            User user = userOptional.get();
            if (applyCertsDTO.getRenew() != null && !applyCertsDTO.getRenew() && user.getClientPfx() != null) {
                return new ApplyCertsVO(Base64.getEncoder().encodeToString(user.getClientPfx()), null);
            }

            byte[] bytes = applyCertsToBinary(configDAO.findById(1).get(), applyCertsDTO.getDays(), user.getEmail(), user.getUsername());

            if (bytes == null)
                return new ApplyCertsVO(null, "Certificate generation failure.");
            user.setCertDays(applyCertsDTO.getDays());
            user.setClientPfx(bytes);
            user.setCertCreateTime(new Date(System.currentTimeMillis()));
            userDAO.save(user);
            return new ApplyCertsVO(Base64.getEncoder().encodeToString(bytes), null);
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return new ApplyCertsVO(null, ErrorUtils.getErrorMessage(e));
        } finally {
            lock.unlock();
        }

    }

    @Override
    public File applyCertsToFile(HttpServletResponse response, Integer days, Boolean renew) {
        lock.lock();
        File file = null;
        try {
            if (UserUtils.currentUser().getUsername().equals("admin")) {
                return null;
            }
            Optional<User> userOptional = userDAO.findById(UserUtils.currentUser().getId());
            if (!userOptional.isPresent())
                return null;
            User user = userOptional.get();
            String fileName = UserUtils.currentUser().getUsername() + ".pfx";
            response.reset();
            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            if (!renew && user.getClientPfx() == null) {
                return null;
            }

            if (renew != null && !renew && user.getClientPfx() != null) {
                file = bytesToRequest(user.getClientPfx(), fileName, response);
                return file;
            }

            byte[] bytes = applyCertsToBinary(configDAO.findById(1).get(), days, user.getEmail(), user.getUsername());
            if (bytes == null)
                return null;
            file = bytesToRequest(bytes, fileName, response);
            if (file == null)
                return null;
            user.setCertDays(days);
            user.setClientPfx(bytes);
            user.setCertCreateTime(new Date(System.currentTimeMillis()));

            userDAO.save(user);
            return file;
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return null;
        } finally {
            if (file != null)
                file.delete();
            lock.unlock();
        }

    }

//    @Override
//    public File applyCertsToFile(HttpServletResponse response, Integer days, Boolean renew) {
//        try {
//            User currentUser = UserUtils.currentUser();
//            if (currentUser.getUsername().equals("admin")) {
//                return null;
//            }
//            Optional<User> userOptional = userDAO.findById(currentUser.getId());
//            if (!userOptional.isPresent())
//                return null;
//            User user = userOptional.get();
//
//            File file = applyCertsToFile(configDAO.findById(1).get(), days, user.getEmail(), user.getUsername());
//            response.reset();
//            response.setContentType("application/octet-stream");
//            response.setCharacterEncoding("utf-8");
//            response.setContentLength((int) file.length());
//            response.setHeader("Content-Disposition", "attachment;filename=" + currentUser.getUsername()+".pfx");
//            try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
//                byte[] buff = new byte[1024];
//                OutputStream os  = response.getOutputStream();
//                int i;
//                while ((i = bis.read(buff)) != -1) {
//                    os.write(buff, 0, i);
//                    os.flush();
//                }
//            } catch (IOException e) {
//                return null;
//            }
//            user.setCertDays(applyCertsDTO.getDays());
//            user.setClientPfx(bytes);
//            userDAO.save(user);
//            return file;
//        } catch (IOException | InterruptedException e) {
//            logger.error("ERROR", "ERROR: ", e);
//            return null;
//        }
//    }

    @Override
    public Iterable<ListFormatVO> listFormat() {
        return FlowConstants.STREAM_FORMAT.entrySet().stream()
                .map(t -> new ListFormatVO(t.getKey(), t.getValue())).collect(Collectors.toList());
    }

    @Override
    public Iterable<String> listSqlTypes() {
        return com.boraydata.common.FlowConstants.SQL_TYPES;
    }

    @Override
    public Iterable<ListStreamTypeVO> listStreamType() {
        return FlowConstants.STREAM_TYPE.entrySet().stream()
                .map(t -> new ListStreamTypeVO(t.getKey(), t.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<ListCompressionVO> listCompression() {
        return FlowConstants.COMPRESSION_TYPES.entrySet().stream()
                .map(c -> new ListCompressionVO(c.getKey(), c.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<ListEncryptionVO> listEncryption() {
        return FlowConstants.ENCRYPTION_TYPES.entrySet().stream()
                .map(e -> new ListEncryptionVO(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    @Override
    public List<ListFlowDefStatusVO> listFlowDefStatus() {
        return Arrays.stream(FlowDefStatus.values())
                .map(v -> new ListFlowDefStatusVO(v.status, v.name())).collect(Collectors.toList());
    }

    /**
     * describe a stream
     */
    @Override
    public FlowDefVO describeStream(String streamName) {
        FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(streamName, UserUtils.currentUser().getId());
        if (flowDef == null)
            return flowDefToVOFailure("`streamName` is null");
        return flowDefToVOSuccess(flowDef);
    }

    @Override
    public FlowDefListFull describeStreamFull(String streamName) {
        FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(streamName, UserUtils.currentUser().getId());
        return flowDefToFlowDefListFull(flowDef);
    }

    /**
     * list all streams
     */
    @Override
    public Iterable<FlowDefList> listStreams() {
        return StreamSupport.stream(flowDefDAO.findAllByAccountId(UserUtils.currentUser().getId()).spliterator(), false).map(f -> {
            FlowDefList flowDefList = new FlowDefList();
            flowDefList.setStreamName(f.getStreamName());
            flowDefList.setFormat(f.getFormat());
            flowDefList.setStreamType(f.getStreamType());
            flowDefList.setShards(f.getShards());
            flowDefList.setConsumers(f.getConsumers());
            flowDefList.setVersion(f.getVersion());
            flowDefList.setStatus(FlowDefStatus.valueOf(f.getStatus()) == null ? null : FlowDefStatus.valueOf(f.getStatus()).name());
            flowDefList.setRetention(f.getRetention());
            flowDefList.setCreateTime(f.getCreateTime());
            flowDefList.setUpdateTime(f.getUpdateTime());
            // set tag
            Iterable<FlowTagsMapping> flowTagsMapping = flowTagsMappingDAO.findByStreamId(f.getStreamId());
            List<String> tagList = new ArrayList<>();
            StreamSupport.stream(flowTagsMapping.spliterator(), false).forEach(t -> {
                if (t != null) {
                    Optional<FlowTag> flowTag = flowTagDAO.findById(t.getTagId());
                    if (flowTag.isPresent())
                        tagList.add(flowTag.get().getName());
                }
            });
            flowDefList.setTag(tagList);

            flowDefList.setWorkflowName(workFlowMappingDAO.findByStreamId(f.getStreamId()).stream()
                    .map(m -> workFlowDAO.findById(m.getFlowId()).get().getFlowName()).collect(Collectors.toList()));
            return flowDefList;
        }).collect(Collectors.toList());
    }

    @Override
    public Iterable<FlowDefListFull> listStreamFull() {
        return StreamSupport.stream(flowDefDAO.findAllByAccountId(UserUtils.currentUser().getId())
                .spliterator(), false)
                .map(this::flowDefToFlowDefListFull).collect(Collectors.toList());
    }

    /**
     * create a stream & create a kafka topic
     */
    @Override
    public FlowDefVO createStream(FlowDefDTO flowDefDTO) {
        try {
            if (flowDefDTO.getStreamName() == null)
                return flowDefToVOFailure("streamName can not be null");

            Long accountID = UserUtils.currentUser().getId();

            if (flowDefDAO.findByStreamNameAndAccountId(flowDefDTO.getStreamName(), accountID) != null)
                return flowDefToVOFailure("Stream already exists");

            if (!ValidateUtils.validateName(flowDefDTO.getStreamName()))
                return flowDefToVOFailure("streamName only English characters, numbers, and symbols (_,-) are allowed");

            if (flowDefDTO.getShards() < 1 || flowDefDTO.getShards() > 5000)
                return flowDefToVOFailure("Kafka number of partitions must be larger than 0 and less than 100. Shards should be larger than 1 and less than 100");

            if (flowDefDTO.getConsumers() > flowDefDTO.getShards()) {
                return flowDefToVOFailure("Parallelism cannot be greater than the number of partitions");
            }

            FlowDef flowDef = flowDefDTOToFlowDef(flowDefDTO, accountID);
            flowDef.setStatus(FlowDefStatus.ENABLE.status);
            flowDef.setCreateTime(new Date(System.currentTimeMillis()));
            flowDef.setUpdateTime(new Date(System.currentTimeMillis()));

            if (flowDef.getStreamType() == 2) {
                flowDef.setRetention(null);
                FlowDef flow = flowDefDAO.save(flowDef);
                if (flowDefDTO.tag != null) {
                    Arrays.stream(flowDefDTO.tag.split(SymbolConstants.COMMA)).forEach(t -> {
                        FlowTag tag = flowTagDAO.findByNameAndAccountID(t, accountID);
                        if (tag == null) {
                            FlowTag newTag = new FlowTag();
                            newTag.setName(t);
                            newTag.setAccountID(accountID);
                            tag = flowTagDAO.save(newTag);
                        }
                        flowTagsMappingDAO.save(new FlowTagsMapping(tag.getTagId(), flow.getStreamId()));
                    });
                }
                return flowDefToVOSuccess(flow);
            }
            try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
                String topic = flowDef.getStreamName();
                if (flowDefDTO.getForce() && kafkaTopicIsExist(adminClient, topic)) {
                    kafkaDeleteTopic(adminClient, Collections.singletonList(topic));
                    adminClient.close();
                    try (AdminClient adminClient1 = AdminClient.create(admin.getConfigurationProperties())) {
                        kafkaCreateTopic(adminClient1, topic, flowDef.getShards());
                        kafkaAlterRetention(adminClient1, topic, flowDef.getRetention());
                    }
                } else {
                    kafkaCreateTopic(adminClient, topic, flowDef.getShards());
                    kafkaAlterRetention(adminClient, topic, flowDef.getRetention());
                }
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), Arrays.toString(e.getStackTrace()));
                return flowDefToVOFailure(e.getMessage());
            }

            FlowDef flow = flowDefDAO.save(flowDef);

            if (flowDefDTO.tag != null) {
                Arrays.stream(flowDefDTO.tag.split(SymbolConstants.COMMA)).forEach(t -> {
                    FlowTag tag = flowTagDAO.findByNameAndAccountID(t, accountID);
                    if (tag == null) {
                        FlowTag newTag = new FlowTag();
                        newTag.setName(t);
                        newTag.setAccountID(accountID);
                        tag = flowTagDAO.save(newTag);
                    }
                    flowTagsMappingDAO.save(new FlowTagsMapping(tag.getTagId(), flow.getStreamId()));
                });
//                List<String> collect = Arrays.stream(flowDefDTO.tag.split(SymbolConstants.COMMA)).map(t -> {
//                    FlowTag tag = flowTagDAO.findByNameAndAccountID(t, accountID);
//                    if (tag == null) {
//                        FlowTag newTag = new FlowTag();
//                        newTag.setName(t);
//                        newTag.setAccountID(accountID);
//                        flowTagDAO.save(newTag);
//                    }
//                    flowTagsMappingDAO.save(new FlowTagsMapping(tag.getTagId(), flow.getStreamId()));
//                    return t;
//                }).filter(Objects::nonNull).collect(Collectors.toList());
//                if (collect.size() > 0) {
//                    StringBuilder t = new StringBuilder();
//                    for (String s : collect) {
//                        t.append(s).append(SymbolConstants.COMMA);
//                    }
//                    return flowDefToVOFailure("`" + t + "` tags does not exist.");
//                }
            }
            return flowDefToVOSuccess(flow);
        } catch (Throwable e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
    }

    /**
     * update a stream
     */
    @Override
    public FlowDefVO updateStream(FlowDefUpdateDTO flowDefUpdateDTO) {

        try {

            if (flowDefUpdateDTO.getStreamName() == null)
                return flowDefToVOFailure("streamName can not be null");

            FlowDef flowDef = flowDefDAO.findByStreamNameAndAccountId(flowDefUpdateDTO.getStreamName(), UserUtils.currentUser().getId());
            if (flowDef == null)
                return flowDefToVOFailure(flowDefUpdateDTO.getStreamName() + " stream does not exist");

            if (FlowDefStatus.ACTIVE.status == flowDef.getStatus())
                return flowDefToVOFailure(flowDefUpdateDTO.getStreamName() + " stream is active");

            if (flowDefUpdateDTO.getVersion() != null) {
                flowDef.setVersion(flowDefUpdateDTO.getVersion());
            }

            if (flowDefUpdateDTO.getWCount() != null) {
                flowDef.setWCount(flowDefUpdateDTO.getWCount());
            }

            if (flowDefUpdateDTO.getWbSize() != null) {
                flowDef.setWbSize(flowDefUpdateDTO.getWbSize());
            }

            if (flowDefUpdateDTO.getRCount() != null) {
                flowDef.setRCount(flowDefUpdateDTO.getRCount());
            }

            if (flowDefUpdateDTO.getRbSize() != null) {
                flowDef.setRbSize(flowDefUpdateDTO.getRbSize());
            }

            if (flowDefUpdateDTO.getRetention() != null) {
                try (AdminClient adminClient = AdminClient.create(admin.getConfigurationProperties())) {
                    kafkaAlterRetention(adminClient, flowDefUpdateDTO.getStreamName(), flowDefUpdateDTO.getRetention());
                } catch (Exception e) {
                    logger.error(e.getMessage(), Arrays.toString(e.getStackTrace()));
                    return flowDefToVOFailure(e.getMessage());
                }
                flowDef.setRetention(flowDefUpdateDTO.getRetention());
            }

            if (flowDefUpdateDTO.getConsumers() != null) {
                flowDef.setConsumers(flowDefUpdateDTO.getConsumers());
            }

            if (flowDefUpdateDTO.getEncryptionType() != null) {
                flowDef.setEncryptionType(flowDefUpdateDTO.getEncryptionType());
            }
            if (flowDefUpdateDTO.getCompression() != null) {
                flowDef.setCompression(flowDefUpdateDTO.getCompression());
            }

            flowDef.setUpdateTime(new Date(System.currentTimeMillis()));

            flowDefDAO.save(flowDef);

            return flowDefToVOSuccess(flowDef);
        } catch (Throwable e) {
            logger.error("ERROR", "ERROR: ", e);
            return flowDefToVOFailure(ErrorUtils.getErrorMessage(e));
        }
    }

    private String generateSchema(Integer format, JSONObject streamSchema) {

        if (format == null) {
            return null;
        }
        if (format < SchemaType.value(csv) && format > SchemaType.value(redis)) {
            return null;
        }
        try {
            return schemaUtil.getSchemaParser().get(SchemaType.valueOf(format)).apply(streamSchema);
        } catch (Exception e) {
            logger.error("ERROR", "ERROR: ", e);
            return null;
        }
    }

    private FlowDefVO flowDefToVOSuccess(FlowDef flowDef) {
        FlowDefVO flowDefVO = new FlowDefVO();
        flowDefVO.setStreamName(flowDef.getStreamName());
        flowDefVO.setFormat(flowDef.getFormat());
        flowDefVO.setStreamType(flowDef.getStreamType());
        flowDefVO.setVersion(flowDef.getVersion());
        flowDefVO.setShards(flowDef.getShards());
        flowDefVO.setWCount(flowDef.getWCount());
        flowDefVO.setWbSize(flowDef.getWbSize());
        flowDefVO.setRCount(flowDef.getRCount());
        flowDefVO.setRbSize(flowDef.getRbSize());
        flowDefVO.setRetention(flowDef.getRetention());
        flowDefVO.setConsumers(flowDef.getConsumers());
        flowDefVO.setStatus(flowDef.getStatus());
        flowDefVO.setEncryptionType(flowDef.getEncryptionType());
        flowDefVO.setCompression(flowDef.getCompression());
        flowDefVO.setCreateTime(flowDef.getCreateTime());
        flowDefVO.setUpdateTime(flowDef.getUpdateTime());
        flowDefVO.setAccountId(flowDef.getAccountId());
        return flowDefVO;
    }

    private FlowDefVO flowDefToVOFailure(String error) {
        FlowDefVO flowDefVO = new FlowDefVO();
        flowDefVO.setStateCode(false);
        flowDefVO.setErrorMessage(error);
        return flowDefVO;
    }

    private FlowDef flowDefDTOToFlowDef(FlowDefDTO flowDefDTO, Long accountID) {
        FlowDef flowDef = new FlowDef();
        flowDef.setStreamName(flowDefDTO.getStreamName());
        flowDef.setFormat(flowDefDTO.getFormat());
        flowDef.setStreamType(flowDefDTO.getStreamType());
        flowDef.setAccountId(accountID);
        flowDef.setVersion(flowDefDTO.getVersion());
        flowDef.setShards(flowDefDTO.getShards());
        flowDef.setWCount(flowDefDTO.getWCount());
        flowDef.setWbSize(flowDefDTO.getWbSize());
        flowDef.setRCount(flowDefDTO.getRCount());
        flowDef.setRbSize(flowDefDTO.getRbSize());
        flowDef.setRetention(flowDefDTO.getRetention());
        flowDef.setConsumers(flowDefDTO.getConsumers());
        flowDef.setStatus(flowDefDTO.getStatus());
        flowDef.setEncryptionType(flowDefDTO.getEncryptionType());
        flowDef.setCompression(flowDefDTO.getCompression());
        flowDef.setStreamSchema(generateSchema(flowDef.getFormat(), flowDefDTO.getStreamSchema()));
        return flowDef;
    }

    private FlowDefListFull flowDefToFlowDefListFull(FlowDef f) {
        FlowDefListFull flowDefList = new FlowDefListFull();
        flowDefList.setStreamId(f.getStreamId());
        flowDefList.setStreamName(f.getStreamName());
        flowDefList.setFormat(f.getFormat());
        flowDefList.setStreamSchema(f.getStreamSchema());
        flowDefList.setStreamType(f.getStreamType());
        flowDefList.setShards(f.getShards());
        flowDefList.setWCount(f.getWCount());
        flowDefList.setWbSize(f.getWbSize());
        flowDefList.setRCount(f.getRCount());
        flowDefList.setRbSize(f.getRbSize());
        flowDefList.setConsumers(f.getConsumers());
        flowDefList.setVersion(f.getVersion());
        flowDefList.setRetention(f.getRetention());
        flowDefList.setStatus(FlowDefStatus.valueOf(f.getStatus()) == null ? null : FlowDefStatus.valueOf(f.getStatus()).name());
        flowDefList.setEncryptionType(f.getEncryptionType());
        flowDefList.setCompression(f.getCompression());
        flowDefList.setCreateTime(f.getCreateTime());
        flowDefList.setUpdateTime(f.getUpdateTime());
        // set tag
        List<String> tagList = new ArrayList<>();
        StreamSupport.stream(flowTagsMappingDAO.findByStreamId(f.getStreamId()).spliterator(), false)
                .forEach(t -> {
                    if (t != null) {
                        Optional<FlowTag> flowTag = flowTagDAO.findById(t.getTagId());
                        flowTag.ifPresent(tag -> tagList.add(tag.getName()));
                    }
                });
        flowDefList.setTag(tagList);
//        FlowTagsMapping flowTagsMapping = flowTagsMappingDAO.findByStreamId(f.getStreamId());
//        if (flowTagsMapping != null) {
//            FlowTag flowTag = flowTagDAO.findByTagIdAndAccountID(flowTagsMapping.getTagId(), f.getAccountId());
//            flowDefList.setTag(flowTag == null ? null : flowTag.getName());
//        } else flowDefList.setTag(null);
        flowDefList.setWorkflowName(workFlowMappingDAO.findByStreamId(f.getStreamId()).stream()
                .map(m -> workFlowDAO.findById(m.getFlowId()).get().getFlowName()).collect(Collectors.toList()));
        return flowDefList;
    }

    private boolean kafkaTopicIsExist(AdminClient adminClient, String streamName) throws ExecutionException, InterruptedException {
        ListTopicsOptions listTopicsOptions = new ListTopicsOptions();
        listTopicsOptions.listInternal(true);
        ListTopicsResult res = adminClient.listTopics(listTopicsOptions);
        return res.names().get().contains(streamName);
    }

    private void kafkaCreateTopic(AdminClient adminClient, String streamName, int shards) throws
            ExecutionException, InterruptedException {
        CreateTopicsResult createTopicsResult = adminClient.createTopics(Collections.singletonList(
                new NewTopic(streamName, shards, (short) 1)));
        createTopicsResult.all().get();
    }

    private void kafkaDeleteTopic(AdminClient adminClient, Collection<String> topics) throws
            ExecutionException, InterruptedException {
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
        deleteTopicsResult.all().get();
    }

    private void kafkaAlterRetention(AdminClient adminClient, String streamName, int retention) throws Exception {
        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, streamName);

        String retentionMs = String.valueOf(retention * 3600000);
        List<AlterConfigOp> value = Collections.singletonList(new AlterConfigOp(new ConfigEntry("retention.ms",
                retentionMs), AlterConfigOp.OpType.SET));
        AlterConfigsResult alterConfigsResult = adminClient.incrementalAlterConfigs(Collections.singletonMap(resource, value));
        alterConfigsResult.all().get();

        DescribeConfigsResult describeConfigsResult = adminClient.describeConfigs(Collections.singletonList(resource));
        Map<ConfigResource, Config> configResourceConfigMap = describeConfigsResult.all().get();
        if (!configResourceConfigMap.get(resource).get("retention.ms").value().equals(retentionMs)) {
            throw new Exception("`retention.ms` alter failure");
        }
    }

    private void kafkaAlterPartition(AdminClient adminClient, String streamName, int shards) throws
            ExecutionException, InterruptedException {
        CreatePartitionsResult createPartitionsResult = adminClient.createPartitions(
                Collections.singletonMap(streamName, NewPartitions.increaseTo(shards)));
        createPartitionsResult.all().get();
    }

    private byte[] applyCertsToBinary(com.boraydata.flowregistry.entity.Config config, Integer days, String email, String username) throws Exception {
        File userDir = new File(username);
        userDir.mkdir();
        try {
            writeFile(username + "/ca.key", config.getCaKey());
            writeFile(username + "/ca.crt", config.getCaCert());
            String bytes = execSSLToBytes(String.valueOf(config.getRsaBits()), String.valueOf(days),
                    username, email);
            if (bytes == null) {
                return Files.readAllBytes(Paths.get(username + "/client.pfx"));
            } else {
                throw new Exception(bytes);
            }
        } finally {
            deleteDir(userDir);
        }
    }

//    private File applyCertsToFile(com.boraydata.flowregistry.entity.Config config, Integer days, String email, String username) throws IOException, InterruptedException {
//        File userDir = new File(username);
//        userDir.mkdir();
//        try {
//            writeFile(username + "/ca.key", config.getCaKey());
//            writeFile(username + "/ca.crt", config.getCaCert());
//            String path = execSSLToPath(String.valueOf(config.getRsaBits()), String.valueOf(days),
//                    username, email);
//            return new File(path);
//        } finally {
////            deleteDir(userDir);
//        }
//    }

    private void writeFile(String fileName, String str) {
        try (FileOutputStream outputStream = new FileOutputStream(fileName)) {
            byte[] strToBytes = str.getBytes();
            outputStream.write(strToBytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] ch = dir.list();
            if (ch != null)
                for (String s : ch) {
                    boolean success = deleteDir(new File(dir, s));
                    if (!success) {
                        return false;
                    }
                }
        }
        return dir.delete();
    }


    private String execSSLToBytes(String rsa, String days, String user, String emailAddress) throws InterruptedException, IOException {
//        if (execSSL(rsa, days, user, emailAddress))
//            return Files.readAllBytes(Paths.get(user + "/client.pfx"));
//        else
//            return null;

        return execSSL(rsa, days, user, emailAddress);
    }

    private String execSSLToPath(String rsa, String days, String user, String emailAddress) throws InterruptedException, IOException {
        return execSSL(rsa, days, user, emailAddress);
        // user + "/client.pfx"
    }

    private String execSSL(String rsa, String days, String user, String emailAddress) throws InterruptedException, IOException {
        String ssl1 = "openssl req -newkey rsa:" + rsa + " -nodes -keyout client.key -out client.csr -subj \"/CN=" + user + "/emailAddress=" + emailAddress + "\"";
        String[] cmd1 = new String[]{"/bin/sh", "-c", "cd " + user + " && " + ssl1};
        Process ps1 = Runtime.getRuntime().exec(cmd1);
        if (ps1.waitFor() != 0) {
            String s = InputStreamToString(ps1.getErrorStream());
            logger.info(s);
            return s;
        }
        String ssl2 = "openssl x509 -req -days " + days + " -in client.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out client.crt";
        String[] cmd2 = new String[]{"/bin/sh", "-c", "cd " + user + " && " + ssl2};
        Process ps2 = Runtime.getRuntime().exec(cmd2);
        if (ps2.waitFor() != 0) {
            String s = InputStreamToString(ps2.getErrorStream());
            logger.info(s);
            return s;
        }
        String ssl3 = "openssl pkcs12 -export -password pass:8393051630 -out client.pfx -inkey client.key -in client.crt -certfile ca.crt";
        String[] cmd3 = new String[]{"/bin/sh", "-c", "cd " + user + " && " + ssl3};
        Process ps3 = Runtime.getRuntime().exec(cmd3);
        if (ps3.waitFor() != 0) {
            String s = InputStreamToString(ps3.getErrorStream());
            logger.info(s);
            return s;
        }
        return null;
    }

    private File bytesToRequest(byte[] bytes, String fileName, HttpServletResponse response) {
        File file = byteToFile(bytes, fileName);
        response.setContentLength((int) file.length());
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buff = new byte[1024];
            OutputStream os = response.getOutputStream();
            int i;
            while ((i = bis.read(buff)) != -1) {
                os.write(buff, 0, i);
                os.flush();
            }
        } catch (Exception e) {
            return null;
        }
        return file;
    }

    private File byteToFile(byte[] bytes, String path) {
        try {
            File localFile = new File(path);
            if (!localFile.exists()) {
                localFile.createNewFile();
            }
            OutputStream os = new FileOutputStream(localFile);
            os.write(bytes);
            os.close();
            return localFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String InputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf8"));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {

            stringBuilder.append("\n");

            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
