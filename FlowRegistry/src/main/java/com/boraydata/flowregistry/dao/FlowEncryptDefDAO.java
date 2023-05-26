package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.FlowEncryptDef;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Repository
public interface FlowEncryptDefDAO extends PagingAndSortingRepository<FlowEncryptDef, String> {
    FlowEncryptDef findByStreamName(String streamName);
}
