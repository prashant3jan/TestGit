package com.springboot2jpacrudexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.springboot2jpacrudexample.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

}
