package com.in28minutes.learning.jpa.jpain10steps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.in28minutes.learning.jpa.jpain10steps.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
