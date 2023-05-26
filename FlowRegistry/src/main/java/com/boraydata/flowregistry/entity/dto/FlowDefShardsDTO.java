package com.boraydata.flowregistry.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowDefShardsDTO {
    private String streamName;
    private Integer shards;
}
