package com.boraydata.workflow.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "workflow")
public class WorkFlow {

    @Id
    @Column(name = "wflowid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long flowId;

    @Column(name = "accountid")
    public Long accountId;

    @Column(name = "wflow_name")
    public String flowName;

    @Column(name = "wflow_cfg", columnDefinition = "text")
    public String flowCfg;

    @Column(name = "createtime", columnDefinition = "timestamp default now()")
    public Date createTime;

    @Column(name = "updatetime", columnDefinition = "timestamp default now() on update now()")
    public Date updateTime;

    @Column(name = "status")
    public Integer  status;

    @Column(name = "master_mem")
    public Integer masterMemory;

    @Column(name = "worker_mem")
    public Integer workerMemory;

    @Column(name = "workerslots")
    public Integer workerSlots;

    @Column(name = "wflow_desc", length = 500)
    public String flowDesc;
}
