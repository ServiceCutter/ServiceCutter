package ch.hsr.servicestoolkit.editor.repository;

import ch.hsr.servicestoolkit.editor.domain.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
