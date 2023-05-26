package com.boraydata.flowregistry.actions;

import com.boraydata.flowregistry.service.FlowDefService;
import com.boraydata.flowregistry.utils.DataSourceProperties;

/**
 * TODO
 *
 * @date: 2021/4/19
 * @author: hatter
 **/
public class FlowDefAction extends DataSourceAction<FlowDefService> {

    protected DataSourceProperties dataSourceProperties;

    protected FlowDefAction(DataSourceProperties dataSourceProperties){
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public FlowDefService getService() {
        return getBean();
    }

    @Override
    FlowDefService getBean() {
        return context.getBean(FlowDefService.class);
    }

}
