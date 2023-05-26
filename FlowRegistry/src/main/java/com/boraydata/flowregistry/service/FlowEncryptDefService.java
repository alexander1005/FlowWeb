package com.boraydata.flowregistry.service;


import com.boraydata.flowregistry.dao.FlowEncryptDefDAO;
import com.boraydata.flowregistry.entity.FlowEncryptDef;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
public interface FlowEncryptDefService extends Service {

    FlowEncryptDef findByStreamName(String streamName);

    Iterable<FlowEncryptDef> findAll();
}
