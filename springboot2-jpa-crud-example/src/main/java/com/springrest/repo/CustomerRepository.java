package com.springrest.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.springrest.model.Customer;

public interface CustomerRepository extends CrudRepository<Customer, Long> {
	List<Customer> findByAge(int age);

}
