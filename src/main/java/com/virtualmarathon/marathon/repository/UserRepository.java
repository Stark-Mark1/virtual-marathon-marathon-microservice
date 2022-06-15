package com.virtualmarathon.marathon.repository;

import com.virtualmarathon.marathon.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class UserRepository {

    @PersistenceContext
    EntityManager entityManager;

    public User findUserByEmail(String email){
        return entityManager.find(User.class,email);
    }
}
