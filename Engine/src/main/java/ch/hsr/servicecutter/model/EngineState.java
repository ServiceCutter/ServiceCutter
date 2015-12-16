package ch.hsr.servicecutter.model;

public class EngineState {
	private String description;

	public EngineState() {
	}

	public EngineState(final String description) {
		this.setDescription(description);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}
}