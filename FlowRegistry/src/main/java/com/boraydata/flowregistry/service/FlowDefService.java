package com.boraydata.flowregistry.service;


import com.boraydata.flowregistry.dao.FlowDefDAO;
import com.boraydata.flowregistry.entity.FlowDef;
import com.boraydata.flowregistry.entity.dto.*;
import com.boraydata.flowregistry.entity.vo.*;
import org.springframework.core.io.FileSystemResource;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
public interface FlowDefService extends Service {

    FlowDef findByStreamName(String streamName);

    Iterable<FlowDef> findAll();

    /**
     * rest
     **/
    FlowDefVO updateShardCount(FlowDefShardsDTO flowDefShardsDTO);

    FlowDefVO updateRetention(FlowDefRetentionDTO flowDefRetentionDTO);

    FlowDefVO updateStatus(FlowDefStatusDTO flowDefStatusDTO);

    FlowDefVO deleteByStreamName(String streamName);

    FlowDefVO deleteAll();

    ApplyCertsVO applyCerts(ApplyCertsDTO applyCertsDTO);

    File applyCertsToFile(HttpServletResponse response, Integer days, Boolean renew);

    Iterable<ListFormatVO> listFormat();

    Iterable<String> listSqlTypes();

    Iterable<ListStreamTypeVO> listStreamType();

    List<ListCompressionVO> listCompression();

    List<ListEncryptionVO> listEncryption();

    List<ListFlowDefStatusVO> listFlowDefStatus();

    Iterable<FlowDefList> listStreams();

    Iterable<FlowDefListFull> listStreamFull();

    FlowDefVO describeStream(String streamName);

    FlowDefListFull describeStreamFull(String streamName);

    FlowDefVO createStream(FlowDefDTO flowDefDTO);

    FlowDefVO updateStream(FlowDefUpdateDTO flowDefUpdateDTO);

}
