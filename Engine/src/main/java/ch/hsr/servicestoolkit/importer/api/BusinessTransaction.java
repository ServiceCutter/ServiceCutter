package ch.hsr.servicestoolkit.importer.api;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class BusinessTransaction {

	private List<String> fieldsRead = new ArrayList<>();
	private List<String> fieldsWritten = new ArrayList<>();
	private String name;
	private double frequency;

	public BusinessTransaction() {
	}

	public BusinessTransaction(final String name, final double frequency) {
		super();
		this.frequency = frequency;
		if (frequency > 1 || frequency < 0) {
			throw new InvalidParameterException("frequency must be between 0 and 1!");
		}
		this.name = name;
	}

	public double getFrequency() {
		return frequency;
	}

	public String getName() {
		return name;
	}

	public List<String> getFieldsRead() {
		return fieldsRead;
	}

	public void setFieldsRead(final List<String> fieldsRead) {
		if (fieldsRead != null) {
			this.fieldsRead.clear();
			this.fieldsRead.addAll(fieldsRead);
		}
	}

	public List<String> getFieldsWritten() {
		return fieldsWritten;
	}

	public void setFieldsWritten(final List<String> fieldsWritten) {
		if (fieldsWritten != null) {
			this.fieldsWritten.clear();
			this.fieldsWritten.addAll(fieldsWritten);
		}
	}

	@Override
	public String toString() {
		return "BusinessTransaction [fieldsRead=" + fieldsRead + ", fieldsWritten=" + fieldsWritten + ", name=" + name + ", frequency=" + frequency + "]";
	}

}
