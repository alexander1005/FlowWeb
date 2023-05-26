package com.boraydata.workflow.entity;

import com.boraydata.workflow.entity.id.FunctionDefId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * TODO
 *
 * @date: 2021/4/23
 * @author: hatter
 **/
@Getter
@Setter
@Entity
@Data
@Table(name = "function_def")
@IdClass(FunctionDefId.class)
public class FunctionDef implements Serializable {

    @Id
    @Column(name = "accountid", nullable = false)
    public Long accountId;

    @Id
    @Column(name = "wflowid", nullable = false)
    public Long workFlowId;

    @Id
    @Column(name = "fname", nullable = false)
    public String functionName;

    @Column(name = "ftype")
    public Integer functionType = 2;

    @Column(name = "fclass")
    public String functionClass;

    @Column(name = "fargs")
    public String functionArgs;

    @Column(name = "fversion")
    public String functionVersion;

    @Column(name = "flocation")
    public String functionLocation;

    @Column(name = "createtime")
    public Date createTime;

    @Column(name = "updatetime")
    public Date updateTime;

    @Column(name = "fdesc", columnDefinition = "text")
    public String fDesc;
}
