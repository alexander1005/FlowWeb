package com.boraydata.workflow.entity.id;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class FunctionDefId implements Serializable {

    public Long accountId;

    public Long workFlowId;

    public String functionName;
}
