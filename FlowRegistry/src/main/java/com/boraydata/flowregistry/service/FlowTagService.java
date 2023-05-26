package com.boraydata.flowregistry.service;


import com.boraydata.flowregistry.entity.FlowTag;
import com.boraydata.flowregistry.entity.FlowTagsMapping;
import com.boraydata.flowregistry.entity.dto.FlowTagDTO;
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateFlowDTO;
import com.boraydata.flowregistry.entity.dto.FlowTagUpdateWorkFlowDTO;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
public interface FlowTagService extends Service {

//    FlowTagVO describeTag(String streamName, String flowName);

    Iterable<FlowTag> listTags();

    Iterable<FlowTagsMapping> listTagsMapping();

    String addTags(FlowTagDTO flowTagDTO);

    String updateFlowTag(FlowTagUpdateFlowDTO flowTagUpdateDTO);

    String updateWorkFlowTag(FlowTagUpdateWorkFlowDTO flowTagUpdateWorkFlowDTO);

//    FlowTagVO updateTag(FlowTagDTO flowTagDTO);

    void deleteTag(String tagName);

}
