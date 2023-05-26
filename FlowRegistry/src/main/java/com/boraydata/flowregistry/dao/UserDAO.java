package com.boraydata.flowregistry.dao;

import com.boraydata.flowregistry.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserDAO extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Transactional
    void deleteByUsername(String username);
}