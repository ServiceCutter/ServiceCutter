package ch.hsr.servicestoolkit.model.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.systemdata.Model;

public interface ModelRepository extends CrudRepository<Model, Long> {

	Model findByName(String name);

}
