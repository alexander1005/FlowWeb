package com.boraydata.flowregistry.entity.vo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class FlowDefList {
    private String streamName;
    private Integer format;
    private Integer streamType;
    private Integer shards;
    private Integer consumers;
    private String version;
    private String status;
    private Integer retention;
    private Date createTime;
    private Date updateTime;
    private List<String> tag;
    private List<String> workflowName;
}
