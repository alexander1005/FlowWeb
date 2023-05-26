package com.boraydata.workflow.service.impl;

import com.boraydata.workflow.dao.DistributedLockInfoDAO;
import com.boraydata.workflow.entity.DistributedLockInfo;
import com.boraydata.workflow.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@Service
public class DistributedLockServiceImpl implements DistributedLockService {

    private final Integer DEFAULT_EXPIRED_SECONDS = 10;

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockServiceImpl.class);


    @Autowired
    private DistributedLockInfoDAO distributedLockInfoDAO;

    @Override
    public boolean tryLock(String tag) {
        try {
            DistributedLockInfo lock = distributedLockInfoDAO.findByTag(tag);
            if (Objects.isNull(lock)) {
                distributedLockInfoDAO.save(new DistributedLockInfo(tag, this.addSeconds(new Date(), DEFAULT_EXPIRED_SECONDS), DistributedLockInfo.LOCKED_STATUS));
                return true;
            } else {
                Date expiredTime = lock.getExpirationTime();
                Date now = new Date();
                if (expiredTime.before(now)) {
                    lock.setExpirationTime(this.addSeconds(now, DEFAULT_EXPIRED_SECONDS));
                    distributedLockInfoDAO.save(lock);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("ERROR: ", e);
            return false;
        }
        return false;
    }

    @Override public void unlock(String tag) {
        distributedLockInfoDAO.deleteByTag(tag);
    }

    private Date addSeconds(Date date, Integer seconds) {
        if (Objects.isNull(seconds)) {
            seconds = DEFAULT_EXPIRED_SECONDS;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }
}
