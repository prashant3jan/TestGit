package com.in28minutes.learning.jpa.jpain10steps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.in28minutes.learning.jpa.jpain10steps.entity.User;
import com.in28minutes.learning.jpa.jpain10steps.repository.UserRepository;
@Component
public class UserRepositoryCommandLineRunner implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(UserDAOServiceCommandLineRunner.class);
	@Autowired
	private UserRepository userRepository;

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		User user = new User("Jill","Admin");
		userRepository.save(user);
		log.info("new user is created"+user);
	}

}
