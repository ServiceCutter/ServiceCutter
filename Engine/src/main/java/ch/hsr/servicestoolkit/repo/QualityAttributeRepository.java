package ch.hsr.servicestoolkit.repo;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.QualityAttribute;

public interface QualityAttributeRepository extends CrudRepository<QualityAttribute, Long> {

}
