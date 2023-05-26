package com.boraydata.workflow.entity.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkFlowUpdateDTO {

    public String flowName;

    public JSONObject flowCfg;

    public Integer status;

    public Integer masterMemory;

    public Integer workerMemory;

    public Integer workerSlots;

    public String flowDesc;
}