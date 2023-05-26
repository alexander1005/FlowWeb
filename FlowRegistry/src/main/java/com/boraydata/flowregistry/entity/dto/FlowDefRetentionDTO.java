package com.boraydata.flowregistry.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowDefRetentionDTO {
    private String streamName;
    private Integer retention;
}
