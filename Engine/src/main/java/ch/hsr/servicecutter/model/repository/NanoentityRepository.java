package ch.hsr.servicecutter.model.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.userdata.Nanoentity;
import ch.hsr.servicecutter.model.userdata.UserSystem;

public interface NanoentityRepository extends CrudRepository<Nanoentity, Long> {

	Nanoentity findByNameAndUserSystem(String name, UserSystem userSystem);

	Nanoentity findByContextAndNameAndUserSystem(final String context, final String name, UserSystem userSystem);

	Set<Nanoentity> findByUserSystem(UserSystem userSystem);

}
