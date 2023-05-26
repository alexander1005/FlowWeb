package com.boraydata.flowregistry.entity.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class FlowDefListFull {
    private String streamId;

    private String streamName;

    private Integer format;

    private String streamSchema;

    private Integer streamType;

    private Long accountId;

    private String version;

    private Integer shards;

    private Integer wCount;

    private Integer wbSize;

    private Integer rCount;

    private Integer rbSize;

    private Integer retention;

    private Integer consumers;

    private String status;

    private Integer encryptionType;

    private Integer compression;

    private Date createTime;

    private Date updateTime;

    private List<String> tag;

    private List<String> workflowName;

}
