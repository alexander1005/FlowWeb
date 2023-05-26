package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.WorkFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface WorkFlowDAO extends JpaRepository<WorkFlow, Long> {

    WorkFlow findByFlowNameAndAccountId(String flowName, Long accountId);

    List<WorkFlow> findAllByAccountId(Long accountId);

    @Transactional
    int deleteByFlowNameAndAccountId(String flowName, Long accountId);
}
