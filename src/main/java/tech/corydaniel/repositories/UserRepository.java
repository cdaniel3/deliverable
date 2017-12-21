package tech.corydaniel.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.corydaniel.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

	public User findUserByUsername(String username);
}
