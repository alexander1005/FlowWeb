package com.boraydata.workflow.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FunctionDefListVO {
    public Long accountId;

    public Long workFlowId;

    public String functionName;

    public Integer functionType;

    public String functionClass;

    public String functionArgs;

    public String functionVersion;

    public String functionLocation;

    public Date createTime;

    public Date updateTime;

    public String description;

    public String fuiconfig;

    public String fDesc;
}
