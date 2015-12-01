package ch.hsr.servicestoolkit.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.NanoEntity;
import ch.hsr.servicestoolkit.model.Model;

public interface DataFieldRepository extends CrudRepository<NanoEntity, Long> {

	NanoEntity findByNameAndModel(String name, Model model);

	NanoEntity findByContextAndNameAndModel(final String context, final String name, Model model);

	Set<NanoEntity> findByModel(Model model);

}
