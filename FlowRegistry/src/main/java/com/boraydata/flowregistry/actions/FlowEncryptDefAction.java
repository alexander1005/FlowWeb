package com.boraydata.flowregistry.actions;

import com.boraydata.flowregistry.service.FlowEncryptDefService;
import com.boraydata.flowregistry.utils.DataSourceProperties;

/**
 * TODO
 *
 * @date: 2021/4/19
 * @author: hatter
 **/
public class FlowEncryptDefAction extends DataSourceAction<FlowEncryptDefService> {

    protected DataSourceProperties dataSourceProperties;

    protected FlowEncryptDefAction(DataSourceProperties dataSourceProperties){
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public FlowEncryptDefService getService() {
        return getBean();
    }

    @Override
    FlowEncryptDefService getBean() {
        return context.getBean(FlowEncryptDefService.class);
    }
}
