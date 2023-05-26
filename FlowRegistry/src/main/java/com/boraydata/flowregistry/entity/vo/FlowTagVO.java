package com.boraydata.flowregistry.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FlowTagVO {

    private String name;

    private Date createTime;

    private Date updateTime;
}
