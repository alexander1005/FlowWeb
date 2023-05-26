package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.Config;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigDAO extends PagingAndSortingRepository<Config, Integer> {

}
