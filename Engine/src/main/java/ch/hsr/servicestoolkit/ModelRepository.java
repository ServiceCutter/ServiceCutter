package ch.hsr.servicestoolkit;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.Model;

public interface ModelRepository extends CrudRepository<Model, Long> {

}
