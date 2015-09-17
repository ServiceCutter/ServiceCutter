package ch.hsr.servicestoolkit.model;

public class EngineState {
	private String description;

	public EngineState() {
	}

	public EngineState(String description) {
		this.setDescription(description);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}