package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class Characteristic {

	private String characteristic;
	private List<String> nanoentities;

	public Characteristic() {
	}

	public Characteristic(final String characteristic, final List<String> nanoentities) {
		super();
		this.characteristic = characteristic;
		this.nanoentities = nanoentities;
	}

	public String getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(final String characteristic) {
		this.characteristic = characteristic;
	}

	public List<String> getNanoentities() {
		return nanoentities;
	}

	public void setNanoentities(final List<String> nanoentities) {
		this.nanoentities = nanoentities;
	}

	@Override
	public String toString() {
		return characteristic;
	}

}
