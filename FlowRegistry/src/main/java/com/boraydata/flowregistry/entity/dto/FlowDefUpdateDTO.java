package com.boraydata.flowregistry.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowDefUpdateDTO {

    private String streamName;

    private String version;

    private Integer wCount;

    private Integer wbSize;

    private Integer rCount;

    private Integer rbSize;

    private Integer retention;

    private Integer consumers;

    private Integer encryptionType;

    private Integer compression;

    public String tag;
}
