package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.FlowTagsMapping;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FlowTagsMappingDAO extends PagingAndSortingRepository<FlowTagsMapping, Long> {

    Iterable<FlowTagsMapping> findByFlowIdAndStreamId(Long flowId, String streamId);

    Iterable<FlowTagsMapping> findByFlowId(Long flowId);

    Iterable<FlowTagsMapping> findByStreamId(String streamId);
}
