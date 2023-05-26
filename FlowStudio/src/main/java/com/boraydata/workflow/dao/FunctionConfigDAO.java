package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.FunctionConfig;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FunctionConfigDAO extends PagingAndSortingRepository<FunctionConfig, Integer> {
    FunctionConfig findByFunctionName(String functionName);
}
