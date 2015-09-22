package ch.hsr.servicestoolkit.solver;

import java.util.List;

public class BoundedContext {

	List<String> dataFields;

	public BoundedContext(List<String> dataFields) {
		super();
		this.dataFields = dataFields;
	}

	public List<String> getDataFields() {
		return dataFields;
	}

}
