package ch.hsr.servicecutter.importer.api;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;

/**
 * Used to import a nanoentity. Has the prefix "Import" to mitigate a name
 * collision with {@link Nanoentity}
 */
public class ImportNanoentity {
	private String name;
	private String context;

	// used by Jackson
	public ImportNanoentity() {
	}

	public ImportNanoentity(final String name, final String context) {
		super();
		this.name = name;
		this.context = context;
	}

	public String getName() {
		return name;
	}

	public String getContext() {
		return context;
	}

}
