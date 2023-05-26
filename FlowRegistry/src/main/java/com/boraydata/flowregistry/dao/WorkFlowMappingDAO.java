package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.WorkFlowMapping;
import com.boraydata.flowregistry.entity.id.WorkFlowMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface WorkFlowMappingDAO extends JpaRepository<WorkFlowMapping, WorkFlowMappingId>  {

    @Transactional
    void deleteByFlowId(Long flowId);

    List<WorkFlowMapping> findByFlowId(Long flowId);

    List<WorkFlowMapping> findByStreamId(String streamId);
}
