package com.boraydata.workflow.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkFlowVO {
    public Long accountId;

    public String flowName;

    public String flowCfg;

    public Date createTime;

    public Date updateTime;
}
