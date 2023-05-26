package com.boraydata.workflow.dao;

import com.boraydata.workflow.entity.FlowTag;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

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
}
