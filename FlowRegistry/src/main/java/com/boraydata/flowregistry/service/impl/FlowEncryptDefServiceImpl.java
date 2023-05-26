package com.boraydata.flowregistry.service.impl;

import com.boraydata.flowregistry.dao.FlowEncryptDefDAO;
import com.boraydata.flowregistry.entity.FlowEncryptDef;
import com.boraydata.flowregistry.service.FlowEncryptDefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Service
public class FlowEncryptDefServiceImpl implements FlowEncryptDefService {

    @Autowired
    private FlowEncryptDefDAO flowEncryptDefDAO;

    @Override
    public FlowEncryptDef findByStreamName(String streamName) {
        return flowEncryptDefDAO.findByStreamName(streamName);
    }

    @Override
    public Iterable<FlowEncryptDef> findAll() {
        return flowEncryptDefDAO.findAll();
    }
}
