package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.FunctionDef;
import com.boraydata.workflow.entity.id.FunctionDefId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface FunctionDefDAO extends JpaRepository<FunctionDef, FunctionDefId> {

    @Transactional
    void deleteByWorkFlowIdAndAccountId(Long workFlowId, Long accountId);

    Iterable<FunctionDef> findByWorkFlowIdAndAccountId(Long workFlowId, Long accountId);

    FunctionDef findByFunctionNameAndAccountIdAndWorkFlowId(String functionName, Long accountId, Long workFlowId);

    Iterable<FunctionDef> findByFunctionLocationAndAccountId(String functionLocation, Long accountId);

}
