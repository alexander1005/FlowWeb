package com.boraydata.flowregistry.actions;

import com.boraydata.flowregistry.common.FlowApplicationContext;
import com.boraydata.flowregistry.service.Service;
import com.boraydata.flowregistry.utils.DataSourceProperties;
import org.springframework.context.ApplicationContext;

/**
 * @className: DataSourceHandler
 * @description: TODO
 * @author: hatter
 * @date: 2021/4/19
 **/
public abstract class DataSourceAction<T extends Service> {

    protected static ApplicationContext context = FlowApplicationContext.CONTEXT.getContext();

    public abstract T getService();

    abstract T getBean();

    public static DataSourceAction createAction(DataSourceProperties dataSourceProperties) {
        switch (dataSourceProperties.getDataSourceTable()) {
            case flowDef:
                return new FlowDefAction(dataSourceProperties);
            case flowEncryptDef:
                return new FlowEncryptDefAction(dataSourceProperties);
            case flowTag:
                return new FlowTagAction(dataSourceProperties);
            default:
                throw new NullPointerException();
        }
    }
}
