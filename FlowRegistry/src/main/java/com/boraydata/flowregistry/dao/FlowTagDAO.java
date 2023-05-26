package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.FlowTag;
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
public interface FlowTagDAO extends PagingAndSortingRepository<FlowTag, Long> {
    FlowTag findByTagIdAndAccountID(long tagId, long accountID);

    FlowTag findByNameAndAccountID(String name, long accountID);

    Iterable<FlowTag> findByAccountID(long accountID);

    @Transactional
    void deleteByNameAndAccountID(String name, long accountID);

//    FlowTag save(FlowTag entity);


}
