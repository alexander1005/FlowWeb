package com.boraydata.workflow.utils;

import com.boraydata.workflow.WorkFlowClient;

import java.util.Properties;

public enum WorkFlowClientInst {

    INSTANCE(PropertiesUtils.PROPERTIES);

    private final WorkFlowClient client;

    WorkFlowClientInst(Properties cfgProps) {
        client = new WorkFlowClient(cfgProps);
    }

    public WorkFlowClient getInstance() {
        return client;
    }

}
