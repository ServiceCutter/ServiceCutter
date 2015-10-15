package ch.hsr.servicestoolkit.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;

public interface DataFieldRepository extends CrudRepository<DataField, Long> {
	DataField findByNameAndModel(String name, Model model);

	DataField findByName(String name);
}
