package ch.hsr.servicestoolkit.repo;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.DataField;

public interface DataFieldRepository extends CrudRepository<DataField, Long> {

	DataField findByName(String name);
}
