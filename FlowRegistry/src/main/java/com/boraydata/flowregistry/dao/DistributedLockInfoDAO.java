package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.DistributedLockInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DistributedLockInfoDAO extends JpaRepository<DistributedLockInfo, Long> {

    DistributedLockInfo findByTag(String tag);

    @Transactional
    void deleteByTag(String tag);
}
