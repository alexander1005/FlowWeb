package com.boraydata.workflow.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Data
@Entity
@Table(name = "distributed_lock_info", uniqueConstraints={@UniqueConstraint(columnNames={"tag"},name = "uk_tag")})
public class DistributedLockInfo {

    public final static Integer LOCKED_STATUS = 1;
    public final static Integer UNLOCKED_STATUS = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tag", nullable = false)
    private String tag;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "expiration_time", nullable = false)
    private Date expirationTime;

    public DistributedLockInfo(String tag, Date expirationTime, Integer status) {
        this.tag = tag;
        this.expirationTime = expirationTime;
        this.status = status;
    }

    public DistributedLockInfo() {
    }

}
