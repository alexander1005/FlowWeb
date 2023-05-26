package com.boraydata.flowregistry;

import com.boraydata.flowregistry.actions.DataSourceAction;
import com.boraydata.flowregistry.service.FlowDefService;
import com.boraydata.flowregistry.utils.DataSourceProperties;
import com.boraydata.flowregistry.utils.DataSourceTable;
import com.boraydata.flowregistry.utils.DataSourceType;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @date: 2021/4/22
 * @author: hatter
 **/

public class DataSourceActionTest {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceActionTest.class);

    @Test
    void testFindAll() {
        DataSourceAction<FlowDefService> dataSourceAction = DataSourceAction.createAction(getProperties());
        FlowDefService flowDefService = dataSourceAction.getService();
        flowDefService.findAll().forEach(d -> {
            logger.info("FindAll ID: " + d.getStreamId());
            logger.info("FindAll Name: " + d.getStreamName());
        });
    }

    @Test
    void testFindByStreamName() {
        DataSourceAction<FlowDefService> dataSourceAction = DataSourceAction.createAction(getProperties());
        FlowDefService flowDefService = dataSourceAction.getService();
        logger.info("FindByStreamName ID: " + flowDefService.findByStreamName("stream2").getStreamId());
    }

    DataSourceProperties getProperties() {
        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setDatasourceUrl("jdbc:mysql://192.168.10.15:13306/spring");
        dataSourceProperties.setDatasourceUser("root");
        dataSourceProperties.setDatasourcePassword("rdpadmin");
        dataSourceProperties.setDatasourceType(DataSourceType.mysql);
        dataSourceProperties.setDataSourceTable(DataSourceTable.flowDef);
        return dataSourceProperties;
    }


}
