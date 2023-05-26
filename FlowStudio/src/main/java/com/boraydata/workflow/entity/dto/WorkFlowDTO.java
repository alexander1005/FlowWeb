package com.boraydata.workflow.entity.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkFlowDTO {

    public String flowName;

    public JSONObject flowCfg;

    public String tag;

    public String flowDesc;

}