package com.boraydata.flowregistry.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class FlowDefVO {

    private String streamName;

    private Integer format;

    private Long accountId;

    private Integer streamType;

    private String version;

    private Integer shards;

    private Integer wCount;

    private Integer wbSize;

    private Integer rCount;

    private Integer rbSize;

    private Integer retention;

    private Integer consumers;

    private Integer status;

    private Integer encryptionType;

    private Integer compression;

    private Date createTime;

    private Date updateTime;

    public Boolean stateCode = true;

    public String errorMessage;
}
