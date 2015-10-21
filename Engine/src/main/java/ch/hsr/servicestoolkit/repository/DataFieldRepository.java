package ch.hsr.servicestoolkit.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.DataField;

public interface DataFieldRepository extends CrudRepository<DataField, Long> {

	DataField findByName(String name);
}
