package ch.hsr.servicecutter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import ch.hsr.servicecutter.model.EngineState;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.UserSystemRepository;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.UserSystem;
import ch.hsr.servicecutter.rest.InvalidRestParam;
import ch.hsr.servicecutter.rest.ResourceNotFoundException;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private UserSystemRepository userSystemsRepository;
	private CouplingInstanceRepository couplingInstanceRepository;

	@Autowired
	public EngineService(final UserSystemRepository systemRepository, final CouplingInstanceRepository couplingInstanceRepository) {
		this.userSystemsRepository = systemRepository;
		this.couplingInstanceRepository = couplingInstanceRepository;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public EngineState message() {
		return new EngineState("Engine is up and running.");
	}

	@GET
	@Path("/systems")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserSystem[] userSystems() {
		List<UserSystem> result = Lists.newArrayList(userSystemsRepository.findAll());
		return result.toArray(new UserSystem[result.size()]);
	}

	@GET
	@Path("/systems/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserSystem getSystem(@PathParam("id") final Long id) {
		UserSystem result = userSystemsRepository.findOne(id);
		if (result == null) {
			throw new ResourceNotFoundException("System with id " + id);
		}
		return result;
	}

	@POST
	@Path("/systems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserSystem createUserSystem(final String systemName) {
		if (systemName == null || systemName.equals("")) {
			throw new InvalidRestParam();
		}
		UserSystem system = new UserSystem();
		system.setName(systemName);

		log.info("created usersystem {} ", systemName);
		userSystemsRepository.save(system);
		return system;
	}

	@DELETE
	@Path("/systems/{id}")
	@Transactional
	public void deleteUserSystem(@PathParam("id") Long id) {
		userSystemsRepository.delete(id);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/couplingdata")
	@Transactional
	public List<CouplingInstance> getSystemCoupling(@PathParam("id") final Long id) {
		List<CouplingInstance> result = new ArrayList<>();
		UserSystem system = userSystemsRepository.findOne(id);
		Set<CouplingInstance> instances = couplingInstanceRepository.findByUserSystem(system);
		result.addAll(instances);
		for (CouplingInstance couplingInstance : instances) {
			// init lazy collection, otherwise you'll get a serialization
			// exception as the transaction is already closed
			couplingInstance.getAllNanoentities().size();
		}
		log.debug("return criteria for system {}: {}", system.getName(), result.toString());
		Collections.sort(result);
		return result;
	}

}
