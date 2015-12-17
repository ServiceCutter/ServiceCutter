package ch.hsr.servicecutter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
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
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import ch.hsr.servicecutter.model.EngineState;
import ch.hsr.servicecutter.model.repository.CouplingInstanceRepository;
import ch.hsr.servicecutter.model.repository.UserSystemRepository;
import ch.hsr.servicecutter.model.usersystem.CouplingInstance;
import ch.hsr.servicecutter.model.usersystem.UserSystem;

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
		return userSystemsRepository.findOne(id);
	}

	@POST
	@Path("/systems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public UserSystem createUserSystem(UserSystem system, @PathParam("modelName") final String modelName) {
		final String finalSystemName = getNameForSystem(system, modelName);
		if (system == null) {
			system = new UserSystem();
		}
		system.setName(finalSystemName);

		log.info("created usersystem {} containing {} nanoentities.", finalSystemName, system.getNanoentities().size());
		userSystemsRepository.save(system);
		return system;
	}

	private String getNameForSystem(final UserSystem system, final String name) {
		String systemName = (system == null || StringUtils.isEmpty(system.getName())) ? null : system.getName();
		if (systemName == null && StringUtils.isEmpty(name)) {
			throw new IllegalArgumentException("no name defined for system");
		} else if (systemName == null && !StringUtils.isEmpty(name)) {
			return name;
		} else if (systemName != null && StringUtils.isEmpty(name)) {
			return systemName;
		} else if (systemName.equals(name)) {
			return systemName;
		} else {
			throw new IllegalArgumentException("inconsistent system name in URI and body object");
		}
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
