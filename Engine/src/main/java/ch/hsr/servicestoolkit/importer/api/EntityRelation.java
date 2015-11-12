package ch.hsr.servicestoolkit.importer.api;

public class EntityRelation {
	private Entity origin;
	private Entity destination;
	private RelationType type;

	// used by Jackson
	public EntityRelation() {
	}

	public EntityRelation(final Entity origin, final Entity destination, final RelationType type) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.type = type;
	}

	public Entity getDestination() {
		return destination;
	}

	public Entity getOrigin() {
		return origin;
	}

	public RelationType getType() {
		return type;
	}

	public static enum RelationType {
		AGGREGATION, COMPOSITION, INHERITANCE
	}
}
