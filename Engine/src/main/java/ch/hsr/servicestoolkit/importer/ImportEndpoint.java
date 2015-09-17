package ch.hsr.servicestoolkit.importer;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ch.hsr.servicestoolkit.importer.api.DomainModel;
import ch.hsr.servicestoolkit.importer.api.EntityRelation;

@Component
@Path("/engine/import")
public class ImportEndpoint {

	private Logger log = LoggerFactory.getLogger(ImportEndpoint.class);

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public String importDomainModel(DomainModel model) {
		for(EntityRelation r :model.getRelations()){
			System.out.println(r.getOrigin().getName() + " - "+ r.getDestination());
		}
		return "model "+model.hashCode() +" has been created";
	}

}
