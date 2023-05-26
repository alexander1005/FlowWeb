package com.boraydata.flowregistry.entity.id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FlowTagsMappingId implements Serializable {
    public Long tagId;

    public String streamId;

    public Long flowId;
}