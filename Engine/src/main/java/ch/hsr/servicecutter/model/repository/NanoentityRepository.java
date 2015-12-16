package ch.hsr.servicecutter.model.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicecutter.model.systemdata.Model;
import ch.hsr.servicecutter.model.systemdata.Nanoentity;

public interface NanoentityRepository extends CrudRepository<Nanoentity, Long> {

	Nanoentity findByNameAndModel(String name, Model model);

	Nanoentity findByContextAndNameAndModel(final String context, final String name, Model model);

	Set<Nanoentity> findByModel(Model model);

}
