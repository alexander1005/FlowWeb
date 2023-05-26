package com.boraydata.flowregistry.entity.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowDefDTO {

    private String streamName;

    private Integer format;

    private JSONObject streamSchema;

    private Integer streamType;

    private String version;

    private Integer shards = 1;

    private Integer wCount = 1000;

    private Integer wbSize = 1;

    private Integer rCount = 2000;

    private Integer rbSize = 2;

    private Integer retention = 24;

    private Integer consumers = 1;

    private Integer status;

    private Integer encryptionType;

    private Integer compression;

    private Boolean force = false;

    public String tag;
}
