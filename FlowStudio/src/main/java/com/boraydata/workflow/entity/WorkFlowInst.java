package com.boraydata.workflow.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "workflowinst")
public class WorkFlowInst {

    @Id
    @Column(name = "wflow_instid")
    public String wFlowInstId;

    @Column(name = "accountid")
    public Long accountId;

    @Column(name = "starttime", columnDefinition = "timestamp default now() on update now()")
    public Date startTime;

    @Column(name = "wflow_name")
    public String flowName;
}
