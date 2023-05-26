package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
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
@Table(name = "flow_tag")
public class FlowTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tagid", nullable = false)
    private Long tagId;

//    @Id
    @Column(name = "accountID", nullable = false)
    private Long accountID;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    public String name;

    @Column(name = "createtime", columnDefinition = "timestamp default now()")
    private Date createTime;

    @Column(name = "updatetime", columnDefinition = "timestamp default now() on update now()")
    private Date updateTime;
}