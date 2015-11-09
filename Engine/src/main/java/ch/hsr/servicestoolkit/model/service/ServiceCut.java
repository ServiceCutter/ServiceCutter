package ch.hsr.servicestoolkit.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.hsr.servicestoolkit.model.DataField;

public class ServiceCut {

	private Collection<Service> services = new ArrayList<>();
	private Map<DataField, Service> serviceCache = new HashMap<>();

	public void addService(Service service) {
		services.add(service);
		addToCache(service);
	}

	public void addService(DataField... dataFields) {
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

	public Service getService(DataField dataField) {
		return serviceCache.get(dataField);
	}

	private void addToCache(Service service) {
		for (DataField field : service.getDataFields()) {
			serviceCache.put(field, service);
		}
	}

}
