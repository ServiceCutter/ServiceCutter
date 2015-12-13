package ch.hsr.servicestoolkit.model.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.Nanoentity;
import ch.hsr.servicestoolkit.model.Model;

public interface NanoentityRepository extends CrudRepository<Nanoentity, Long> {

	Nanoentity findByNameAndModel(String name, Model model);

	Nanoentity findByContextAndNameAndModel(final String context, final String name, Model model);

	Set<Nanoentity> findByModel(Model model);

}
