package com.boraydata.workflow.entity.vo;

import com.boraydata.workflow.entity.State;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkFlowListVO {

    public Long flowId;

    public String flowName;

    public String flowCfg;

    public Date createTime;

    public Date updateTime;

    public State state;

    public String status;

    public Integer masterMemory;

    public Integer workerMemory;

    public Integer workerSlots;

    public String flowDesc;

    public Iterable<String> flows;

    public Iterable<String> tag;

}
