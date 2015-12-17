package ch.hsr.servicecutter.importer;

import java.util.List;

public class ImportResult {

	private String message;
	private List<String> warnings;
	private Long id;

	public ImportResult() {

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(final List<String> warnings) {
		this.warnings = warnings;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long systemId) {
		this.id = systemId;
	}

}
