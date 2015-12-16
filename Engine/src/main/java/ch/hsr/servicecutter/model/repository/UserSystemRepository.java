package ch.hsr.servicecutter.model.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.usersystem.UserSystem;

public interface UserSystemRepository extends CrudRepository<UserSystem, Long> {

	UserSystem findByName(String name);

}
