package ch.hsr.servicestoolkit.repository;

import org.springframework.data.repository.CrudRepository;

import ch.hsr.servicestoolkit.model.QualityAttribute;

public interface QualityAttributeRepository extends CrudRepository<QualityAttribute, Long> {

}
