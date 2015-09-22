package ch.hsr.servicestoolkit.importer;

import java.util.Date;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.EntityAttribute;
import ch.hsr.servicestoolkit.importer.api.EntityModel;
import ch.hsr.servicestoolkit.model.DataField;
import ch.hsr.servicestoolkit.model.Model;
import ch.hsr.servicestoolkit.repository.ModelRepository;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private final Logger log = LoggerFactory.getLogger(ImportEndpoint.class);
	private final ModelRepository modelRepository;

	@Autowired
	public ImportEndpoint(ModelRepository modelRepository) {
		this.modelRepository = modelRepository;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Transactional
	public String importDomainModel(DomainModel domainModel) {
		Assert.notNull(domainModel);
		Model model = new Model();
		model.setName("imported " + new Date().toString());
		for (EntityModel entityModel : domainModel.getEntities()) {
			for (EntityAttribute entityAttribute : entityModel.getAttributes()) {
				DataField dataField = new DataField();
				dataField.setName(entityAttribute.getName());
				model.addDataField(dataField);
				log.info("added data field '{}' on entity '{}'", dataField.getName(), entityModel.getName());
			}
		}
		modelRepository.save(model);
		// for (EntityRelation r : domainModel.getRelations()) {
		// log.debug("{} - {}", r.getOrigin().getName(), r.getDestination());
		// }
		return "model " + model.getId() + " has been created";
	}

}
