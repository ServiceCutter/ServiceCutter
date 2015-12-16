package ch.hsr.servicecutter.model.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;

public class Service {

	private Set<Nanoentity> nanoentities = new HashSet<>();

	public Service() {
	}

	public Service(Nanoentity... nanoentities) {
		Collections.addAll(this.nanoentities, nanoentities);
	}

	public Set<Nanoentity> getNanoentities() {
		return nanoentities;
	}

	public void setNanoentities(Set<Nanoentity> nanoentities) {
		this.nanoentities = nanoentities;
	}

	public String getNanoentityNames() {
		return nanoentities.stream().map(Nanoentity::getName).collect(Collectors.joining(","));
	}
}
