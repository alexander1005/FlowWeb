package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.WorkFlowInst;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface WorkFlowInstDAO extends JpaRepository<WorkFlowInst, String> {

//    WorkFlowInst findByFlowName(String flowName);

    WorkFlowInst findByFlowNameAndAccountId(String flowName, Long accountId);

    List<WorkFlowInst> findAllByAccountId(Long accountId);

    @Transactional
    int deleteByFlowNameAndAccountId(String flowName, Long accountId);
}
