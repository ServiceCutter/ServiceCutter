package ch.hsr.servicecutter.model.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.systemdata.Model;

public interface ModelRepository extends CrudRepository<Model, Long> {

	Model findByName(String name);

}
