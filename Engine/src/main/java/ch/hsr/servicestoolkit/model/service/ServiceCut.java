package ch.hsr.servicestoolkit.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.hsr.servicestoolkit.model.NanoEntity;

public class ServiceCut {

	private Collection<Service> services = new ArrayList<>();
	private Map<NanoEntity, Service> serviceCache = new HashMap<>();

	public void addService(Service service) {
		services.add(service);
		addToCache(service);
	}

	public void addService(NanoEntity... dataFields) {
		addService(new Service(dataFields));
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

	public Service getService(NanoEntity dataField) {
		return serviceCache.get(dataField);
	}

	private void addToCache(Service service) {
		for (NanoEntity field : service.getDataFields()) {
			serviceCache.put(field, service);
		}
	}

}
