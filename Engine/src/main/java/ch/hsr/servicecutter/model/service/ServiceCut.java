package ch.hsr.servicecutter.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;

public class ServiceCut {

	private Collection<Service> services = new ArrayList<>();
	private Map<Nanoentity, Service> serviceCache = new HashMap<>();

	public void addService(Service service) {
		services.add(service);
		addToCache(service);
	}

	public void addService(Nanoentity... nanoentities) {
		addService(new Service(nanoentities));
	}

	public void setServices(Collection<Service> services) {
		this.services.clear();
		this.serviceCache.clear();
		for (Service service : services) {
			addService(service);
		}
	}

	public Collection<Service> getServices() {
		return Collections.unmodifiableCollection(services);
	}

	public Service getService(Nanoentity nanoentity) {
		return serviceCache.get(nanoentity);
	}

	private void addToCache(Service service) {
		for (Nanoentity nanoentity : service.getNanoentities()) {
			serviceCache.put(nanoentity, service);
		}
	}

}
