package com.boraydata.flowregistry.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

@Getter
@Setter
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    public Long id;

    // 账号
    public String username;

    // 密码
    public String password;

    // 创建用户id
    private Integer createUser;

    // 创建时间
    @Column(columnDefinition = "timestamp default now()")
    private Date createTime;

    // 更新用户id
    private Integer updateUser;

    // 更新时间
    @Column(columnDefinition = "timestamp default now() on update now()")
    private Date updateTime;

    // 是否启用，（0：禁用，1：启用）
    private Boolean enabled;

    @Column(name = "email", length = 100)
    public String email;

    @Column(name = "client_pfx", columnDefinition = "BLOB")
    public byte[] clientPfx;

    @Column(name = "cert_days")
    public Integer certDays;

    @Column(name = "cert_create_time")
    public Date certCreateTime;

    @Column(name = "approval")
    public boolean approval = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}