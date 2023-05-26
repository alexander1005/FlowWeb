package com.boraydata.workflow.entity.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkFlowDeployDTO {

    public String flowName;

    public Integer masterMemory;

    public Integer workerMemory;

    public Integer workerSlots;
}
