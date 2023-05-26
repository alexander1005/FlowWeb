package com.boraydata.flowregistry.service.impl;


import com.boraydata.flowauth.constants.SymbolConstants;
import com.boraydata.flowauth.utils.ValidateUtils;
import com.boraydata.flowregistry.dao.FlowDefDAO;
import com.boraydata.flowregistry.dao.FlowTagDAO;
import com.boraydata.flowregistry.dao.FlowTagsMappingDAO;
import com.boraydata.flowregistry.dao.WorkFlowDAO;
import com.boraydata.flowregistry.entity.FlowDef;
import com.boraydata.flowregistry.entity.FlowTag;
import com.boraydata.flowregistry.entity.FlowTagsMapping;
import com.boraydata.flowregistry.entity.WorkFlow;
import com.boraydata.flowregistry.entity.dto.FlowTagDTO;
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateFlowDTO;
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateWorkFlowDTO;
import com.boraydata.flowregistry.entity.vo.FlowTagVO;
import com.boraydata.flowregistry.service.FlowTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.boraydata.flowregistry.utils.UserUtils.currentUser;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Service
public class FlowTagServiceImpl implements FlowTagService {

    @Autowired
    private FlowDefDAO flowDefDAO;

    @Autowired
    private WorkFlowDAO workFlowDAO;

    @Autowired
    private FlowTagDAO flowTagDAO;

    @Autowired
    private FlowTagsMappingDAO flowTagsMappingDAO;

//    @Override
//    public FlowTagVO describeTag(String streamName, String flowName) {
//        FlowTagsMapping flowTagsMapping = flowTagsMappingDAO.findByFlowIdAndStreamId(getFlowId(flowName), getStreamId(streamName));
//        FlowTag flowTag = flowTagDAO.findByTagIdAndAccountID(flowTagsMapping.getTagId(), UserUtils.currentUser().getId());
//        return flowTagVOToFlowTag(flowTag);
//    }

    @Override
    public Iterable<FlowTag> listTags() {
        return flowTagDAO.findByAccountID(currentUser().getId());
    }

    @Override
    public Iterable<FlowTagsMapping> listTagsMapping() {
        Iterable<FlowTagsMapping> tagsMappings = flowTagsMappingDAO.findAll();
        return StreamSupport.stream(tagsMappings.spliterator(), false).map(m -> {
            if (m.flowId == -1L && m.streamId.equals("null"))
                return null;
            if (m.flowId == -1L)
                return new FlowTagsMapping(m.tagId, m.streamId, null);
            if (m.streamId.equals("null"))
                return new FlowTagsMapping(m.tagId, null, m.flowId);
            return new FlowTagsMapping(m.tagId, m.streamId, m.flowId);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String addTags(FlowTagDTO flowTagDTO) {
        long accountId = currentUser().getId();
        if (flowTagDAO.findByNameAndAccountID(flowTagDTO.getName(), accountId) != null) {
            return "tag already exists";
        }
        if (!ValidateUtils.validateName(flowTagDTO.getName()))
            return "ERROR: tag name only English characters, numbers, and symbols (_,-) are allowed.";

        FlowTag flowTag = new FlowTag();
        flowTag.setName(flowTagDTO.getName());
        flowTag.setAccountID(accountId);
        flowTag.setCreateTime(new Date(System.currentTimeMillis()));
        flowTag.setUpdateTime(new Date(System.currentTimeMillis()));
        flowTagDAO.save(flowTag);
        return null;
    }

//    @Override
//    public void addTagsToStream(FlowTagDTO flowTagDTO) {
//        FlowTag flowTag = new FlowTag();
//        flowTag.setName(flowTagDTO.getName());
//        flowTag.setAccountID(UserUtils.currentUser().getId());
//        flowTag.setCreateTime(new Date(System.currentTimeMillis()));
//        flowTag.setUpdateTime(new Date(System.currentTimeMillis()));
//        FlowTag flowTagSave = flowTagDAO.save(flowTag);
//        FlowTagsMapping flowTagsMapping = new FlowTagsMapping();
//        flowTagsMapping.setFlowId(getFlowId(flowTagDTO.getFlowName()));
//        flowTagsMapping.setStreamId(getStreamId(flowTagDTO.getStreamName()));
//        flowTagsMapping.setTagId(flowTagSave.getTagId());
//        flowTagsMappingDAO.save(flowTagsMapping);
//    }

    @Override
    public String updateFlowTag(FlowTagUpdateFlowDTO flowTagUpdateFlowDTO) {
        long accountID = currentUser().getId();
        FlowDef flow = flowDefDAO.findByStreamNameAndAccountId(flowTagUpdateFlowDTO.getFlowName(), accountID);
        if (flow == null)
            return "ERROR: flow `"+flowTagUpdateFlowDTO.getFlowName()+"` not exist";
        final Iterable<FlowTagsMapping> byStreamId = flowTagsMappingDAO.findByStreamId(flow.getStreamId());
        List<String> tableTag = StreamSupport.stream(byStreamId.spliterator(), true)
                .map(f -> flowTagDAO.findById(f.getTagId()).map(FlowTag::getName).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> userTag = Arrays.stream(flowTagUpdateFlowDTO.getTags().split(SymbolConstants.COMMA)).map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());
        List<String> newTags = userTag.stream().filter(t -> !tableTag.contains(t)).collect(Collectors.toList());
        List<String> delTags = tableTag.stream().filter(t -> !userTag.contains(t)).collect(Collectors.toList());
        final StringBuilder result = new StringBuilder("Add new tags: ");
        newTags.forEach(n -> {
            FlowTag tag = flowTagDAO.findByNameAndAccountID(n, accountID);
            if (tag == null) {
                FlowTag newTag = new FlowTag();
                newTag.setName(n);
                newTag.setAccountID(accountID);
                tag = flowTagDAO.save(newTag);
            }
            result.append(tag.getName()).append(SymbolConstants.COMMA);
            flowTagsMappingDAO.save(new FlowTagsMapping(tag.getTagId(), flow.getStreamId()));
        });
        result.deleteCharAt(result.length()-1);
        result.append(" .  Delete tags: ");
        delTags.forEach(d -> {
            FlowTag tag = flowTagDAO.findByNameAndAccountID(d, accountID);
            if (tag!=null) {
                flowTagsMappingDAO.deleteByTagIdAndStreamId(tag.getTagId(), flow.getStreamId());
                result.append(tag.getName()).append(SymbolConstants.COMMA);
            }
        });
        result.deleteCharAt(result.length()-1);
        return result.toString();
    }

    @Override
    public String updateWorkFlowTag(FlowTagUpdateWorkFlowDTO flowTagUpdateWorkFlowDTO) {
        long accountID = currentUser().getId();
        WorkFlow workFlow = workFlowDAO.findByFlowNameAndAccountId(flowTagUpdateWorkFlowDTO.getWorkflowName(), accountID);
        if (workFlow == null)
            return "ERROR: workFlow `"+flowTagUpdateWorkFlowDTO.getWorkflowName()+"` not exist";
        List<String> tableTag = StreamSupport.stream(flowTagsMappingDAO.findByFlowId(workFlow.getFlowId()).spliterator(), true) //
                .map(f -> flowTagDAO.findById(f.getTagId()).map(FlowTag::getName).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<String> userTag = Arrays.stream(flowTagUpdateWorkFlowDTO.getTags().split(SymbolConstants.COMMA)).map(String::trim).filter(t -> !t.isEmpty()).collect(Collectors.toList());
        List<String> newTags = userTag.stream().filter(t -> !tableTag.contains(t)).collect(Collectors.toList());
        List<String> delTags = tableTag.stream().filter(t -> !userTag.contains(t)).collect(Collectors.toList());
        StringBuilder result = new StringBuilder("Add new tags: ");
        newTags.forEach(n -> {
            FlowTag tag = flowTagDAO.findByNameAndAccountID(n, accountID);
            if (tag == null) {
                FlowTag newTag = new FlowTag();
                newTag.setName(n);
                newTag.setAccountID(accountID);
                tag = flowTagDAO.save(newTag);
            }
            result.append(tag.getName()).append(SymbolConstants.COMMA);
            flowTagsMappingDAO.save(new FlowTagsMapping(tag.getTagId(), workFlow.getFlowId())); //
        });
        result.deleteCharAt(result.length()-1);
        result.append(".  Delete tags: ");
        delTags.forEach(d -> {
            FlowTag tag = flowTagDAO.findByNameAndAccountID(d, accountID);
            if (tag!=null) {
                flowTagsMappingDAO.deleteByTagIdAndFlowId(tag.getTagId(), workFlow.getFlowId()); //
                result.append(tag.getName()).append(SymbolConstants.COMMA);
            }
        });
        result.deleteCharAt(result.length()-1);
        return result.toString();
    }

    @Override
    public void deleteTag(String tagName) {
        flowTagDAO.deleteByNameAndAccountID(tagName, currentUser().getId());

//        Iterable<FlowTagsMapping>  flowTagsMapping = flowTagsMappingDAO.findByTagId(flowTag.getTagId());
//        FlowTag flowTag = flowTagDAO.findByTagIdAndAccountID(flowTagsMapping.getTagId(), UserUtils.currentUser().getId());
//        flowTagDAO.delete(flowTag);
    }

    private String getStreamId(String streamName) {
        return flowDefDAO.findByStreamNameAndAccountId(streamName, currentUser().getId()).getStreamId();
    }

    private Long getFlowId(String flowName) {
        return workFlowDAO.findByFlowNameAndAccountId(flowName, currentUser().getId()).getFlowId();
    }

    private Iterable<FlowTagsMapping> getFlowTagsMapping(String streamName, String flowName) {
        if (flowName != null && streamName != null)
            return flowTagsMappingDAO.findByFlowIdAndStreamId(getFlowId(flowName), getStreamId(streamName));
        if (flowName != null)
            return flowTagsMappingDAO.findByFlowId(getFlowId(flowName));
        if (streamName != null)
            return flowTagsMappingDAO.findByStreamId(getStreamId(streamName));
        return null;
    }

    private FlowTagVO flowTagVOToFlowTag(FlowTag flowTag) {
        FlowTagVO flowTagVO = new FlowTagVO();
        flowTagVO.setName(flowTag.getName());
        flowTagVO.setCreateTime(flowTag.getCreateTime());
        flowTagVO.setUpdateTime(flowTag.getUpdateTime());
        return flowTagVO;
    }
}
