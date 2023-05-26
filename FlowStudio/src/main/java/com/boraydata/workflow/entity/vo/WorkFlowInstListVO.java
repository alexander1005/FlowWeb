package com.boraydata.workflow.entity.vo;

import com.boraydata.workflow.entity.State;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class WorkFlowInstListVO {
    public String wFlowInstId;

    public Date startTime;

    public String flowName;

    public State status;
}
