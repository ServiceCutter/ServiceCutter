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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataFields == null) ? 0 : dataFields.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoundedContext other = (BoundedContext) obj;
		if (dataFields == null) {
			if (other.dataFields != null)
				return false;
		} else if (!dataFields.equals(other.dataFields))
			return false;
		return true;
	}

}
