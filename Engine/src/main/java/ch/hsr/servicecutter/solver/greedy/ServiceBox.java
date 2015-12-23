package ch.hsr.servicecutter.solver.greedy;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;

public class ServiceBox {

	private Logger log = LoggerFactory.getLogger(ServiceBox.class);
	private Set<Nanoentity> currentNanoentities = new HashSet<>();

	public Set<Nanoentity> getCurrentNanoentities() {
		return currentNanoentities;
	}

	public void add(final Nanoentity nanoentity) {
		if (currentNanoentities.contains(nanoentity)) {
			log.warn("{} already exists in ServiceBox!", nanoentity);
		}
		currentNanoentities.add(nanoentity);
	}

	public void remove(final Nanoentity nanoentity) {
		if (!currentNanoentities.remove(nanoentity)) {
			log.warn("{} does not exist in ServiceBox", nanoentity);
		}
	}
}
