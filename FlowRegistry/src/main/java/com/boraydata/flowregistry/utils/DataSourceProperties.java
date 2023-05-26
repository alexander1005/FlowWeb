package com.boraydata.flowregistry.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO
 *
 * @date: 2021/4/19
 * @author: hatter
 **/
@Getter
@Setter
public class DataSourceProperties {
    private String datasourceUrl;
    private String datasourceUser;
    private String datasourcePassword;
    private DataSourceType datasourceType;
    private DataSourceTable dataSourceTable;
}
