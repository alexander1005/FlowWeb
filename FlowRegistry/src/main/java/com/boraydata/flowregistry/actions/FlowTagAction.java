package com.boraydata.flowregistry.actions;

import com.boraydata.flowregistry.service.FlowTagService;
import com.boraydata.flowregistry.utils.DataSourceProperties;

/**
 * TODO
 *
 * @date: 2021/4/19
 * @author: hatter
 **/
public class FlowTagAction extends DataSourceAction<FlowTagService> {

    protected DataSourceProperties dataSourceProperties;

    protected FlowTagAction(DataSourceProperties dataSourceProperties){
        this.dataSourceProperties = dataSourceProperties;
    }

    @Override
    public FlowTagService getService() {
        return getBean();
    }

    @Override
    FlowTagService getBean() {
        return context.getBean(FlowTagService.class);
    }
}
