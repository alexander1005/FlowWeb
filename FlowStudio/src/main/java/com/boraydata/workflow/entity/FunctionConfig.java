package com.boraydata.workflow.entity;

import lombok.Data;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Setter
@Entity
@Data
@Table(name = "function_config")
public class FunctionConfig {
    @Id
    @Column(name = "funcid")
    public Integer functionId;

    @Column(name = "fname")
    public String functionName;

    @Column(name = "ftype")
    public Integer functionType;

    @Column(name = "description")
    public String description;

    @Column(name = "fuiconfig")
    public String fuiconfig;
}
