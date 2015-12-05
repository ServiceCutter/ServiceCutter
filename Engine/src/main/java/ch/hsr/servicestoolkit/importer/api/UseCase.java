package ch.hsr.servicestoolkit.importer.api;

import java.util.ArrayList;
import java.util.List;

public class UseCase {

	private List<String> fieldsRead = new ArrayList<>();
	private List<String> fieldsWritten = new ArrayList<>();
	private String name;

	public UseCase() {
	}

	public UseCase(final String name) {
		super();
		this.name = name;
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
		return "BusinessTransaction [fieldsRead=" + fieldsRead + ", fieldsWritten=" + fieldsWritten + ", name=" + name + "]";
	}

}
