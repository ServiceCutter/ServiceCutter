package ch.hsr.servicestoolkit.model.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import ch.hsr.servicestoolkit.model.DataField;

public class ServiceCut {

	private Collection<Service> services = new ArrayList<>();

	public void addService(Service service) {
		services.add(service);
	}

	public void addService(DataField... dataFields) {
		services.add(new Service(dataFields));
	}

	public Collection<Service> getServices() {
		return Collections.unmodifiableCollection(services);
	}

	public void setServices(Collection<Service> services) {
		this.services.clear();
		this.services.addAll(services);
	}

}
