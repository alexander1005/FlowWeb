package com.boraydata.workflow.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkFlowLogVO {

    public WorkFlowLogVO(String log) {
        this.log = log;
    }

    public String log;
}
