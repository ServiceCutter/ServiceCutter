package ch.hsr.servicecutter.model.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.userdata.UserSystem;

public interface UserSystemRepository extends CrudRepository<UserSystem, Long> {

	UserSystem findByName(String name);

}
