package com.boraydata.workflow.entity.id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class WorkFlowMappingId implements Serializable {
    public String streamId;
    public Long flowId;
}
