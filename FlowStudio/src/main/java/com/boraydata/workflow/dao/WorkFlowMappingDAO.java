package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.WorkFlowMapping;
import com.boraydata.workflow.entity.id.WorkFlowMappingId;
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
