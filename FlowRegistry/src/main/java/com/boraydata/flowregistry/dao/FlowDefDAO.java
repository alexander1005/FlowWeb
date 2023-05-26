package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.FlowDef;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Repository
public interface FlowDefDAO extends PagingAndSortingRepository<FlowDef, String> {

    Iterable<FlowDef> findAllByAccountId(Long accountId);

    FlowDef findByStreamName(String streamName);

    FlowDef findByStreamNameAndAccountId(String streamName, Long accountId);

    @Transactional
    int deleteByStreamNameAndAccountId(String streamName, Long accountId);

    @Transactional
    void deleteAllByAccountId(Long accountId);

//    @Transactional
//    int deleteByStreamName(String streamName);
}
