package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Getter
@Setter
@Entity
public class FlowEncryptDef {

    @Id
    @Column(name = "stream_name", nullable = false, unique = true)
    private String streamName;

    @Column(name = "cer_content")
    private byte[] cerContent;
}
