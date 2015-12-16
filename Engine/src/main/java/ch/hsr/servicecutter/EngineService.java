package ch.hsr.servicecutter;

import java.util.List;

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

import ch.hsr.servicecutter.model.repository.UserSystemRepository;
import ch.hsr.servicecutter.model.userdata.UserSystem;

@Component
@Path("/engine")
public class EngineService {

	private Logger log = LoggerFactory.getLogger(EngineService.class);
	private UserSystemRepository userSystemsRepository;

	@Autowired
	public EngineService(final UserSystemRepository systemRepository) {
		this.userSystemsRepository = systemRepository;
	}

	@GET
	@Path("/models")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserSystem[] userSystems() {
		List<UserSystem> result = Lists.newArrayList(userSystemsRepository.findAll());
		return result.toArray(new UserSystem[result.size()]);
	}

	@GET
	@Path("/models/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public UserSystem getModel(@PathParam("id") final Long id) {
		return userSystemsRepository.findOne(id);
	}

	@POST
	@Path("/models")
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

}
