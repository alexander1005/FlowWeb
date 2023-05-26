package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Destination {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "template", columnDefinition = "text", nullable = false)
    private String template;

    @Column(name = "title", length = 30, nullable = false)
    private String title;
}