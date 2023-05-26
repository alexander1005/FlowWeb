package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * TODO
 *
 * @date: 2021/4/13
 * @author: hatter
 **/
@Getter
@Setter
@Entity
public class FlowDef {

    @Id
    @Column(name = "streamid", length = 36)
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid2")
    private String streamId;

    @Column(name = "stream_name", nullable = false)
    private String streamName;

    @Column(name = "format")
    private Integer format;

    @Column(name = "stream_schema", columnDefinition = "text")
    private String streamSchema;

    @Column(name = "stream_type")
    private Integer streamType;

    @Column(name = "accountID", nullable = false)
    private Long accountId = 1L;

    @Column(name = "version", length = 10)
    private String version;

    @Column(name = "shards", nullable = false)
    private Integer shards = 1;

    @Column(name = "wcount", nullable = false)
    private Integer wCount = 1000;

    @Column(name = "wbsize", nullable = false)
    private Integer wbSize = 1;

    @Column(name = "rcount", nullable = false)
    private Integer rCount = 2000;

    @Column(name = "rbsize", nullable = false)
    private Integer rbSize = 2;

    @Column(name = "retention")
    private Integer retention = 24;

    @Column(name = "consumers", nullable = false)
    private Integer consumers = 1;

    @Column(name = "status")
    private Integer status;

    @Column(name = "encryptiontype")
    private Integer encryptionType;

    @Column(name = "compression")
    private Integer compression;

    @Column(name = "createtime", columnDefinition = "timestamp default now()")
    private Date createTime;

    @Column(name = "updatetime", columnDefinition = "timestamp default now() on update now()")
    private Date updateTime;
}
