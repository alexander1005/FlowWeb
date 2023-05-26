package com.boraydata.workflow.entity.vo;

import com.boraydata.workflow.entity.State;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkFlowDeployVO {
    public String applicationId;
    public State state;
}
