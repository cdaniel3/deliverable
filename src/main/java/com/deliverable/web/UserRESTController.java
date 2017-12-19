package com.deliverable.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deliverable.model.User;
import com.deliverable.repositories.UserRepository;

@RestController
@RequestMapping("/users")
public class UserRESTController {

	@Autowired
	private UserRepository userRepository;
	
	@GetMapping
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}
}
