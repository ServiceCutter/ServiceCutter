package ch.hsr.servicestoolkit.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;

public interface DataFieldRepository extends CrudRepository<DataField, Long> {

	DataField findByNameAndModel(String name, Model model);

	DataField findByContextAndNameAndModel(final String context, final String name, Model model);

	Set<DataField> findByModel(Model model);

}
