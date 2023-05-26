package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.FlowTagsMapping;
import com.boraydata.flowregistry.entity.id.FlowTagsMappingId;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.transaction.Transactional;
import java.util.Optional;

public interface FlowTagsMappingDAO extends PagingAndSortingRepository<FlowTagsMapping, Long> {

    Iterable<FlowTagsMapping> findByFlowIdAndStreamId(Long flowId, String streamId);

    Iterable<FlowTagsMapping> findByFlowId(Long flowId);

    Iterable<FlowTagsMapping> findByStreamId(String streamId);

    Iterable<FlowTagsMapping> findByTagId(Long tagId);

    @Transactional
    void deleteByTagIdAndFlowId(Long tagId, Long flowId);

    @Transactional
    void deleteByTagIdAndStreamId(Long tagId, String streamId);
}
