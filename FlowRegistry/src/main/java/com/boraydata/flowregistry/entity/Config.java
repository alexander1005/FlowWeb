package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Config {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "ca_key")
    private String caKey;

    @Column(name = "ca_cert")
    private String caCert;

    @Column(name = "server_key")
    private String serverKey;

    @Column(name = "server_cert")
    private String serverCert;

    @Column(name = "rsa_bits")
    private Integer rsaBits;

    @Column(name = "subject")
    private String subject;

    @Column(name = "days")
    private Integer days;
}
