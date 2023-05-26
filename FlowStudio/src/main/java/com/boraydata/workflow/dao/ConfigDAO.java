package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.Config;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDAO extends PagingAndSortingRepository<Config, Integer> {

}
