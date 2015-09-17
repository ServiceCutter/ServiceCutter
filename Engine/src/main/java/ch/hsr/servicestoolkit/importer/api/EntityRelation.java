package ch.hsr.servicestoolkit.importer.api;

public class EntityRelation {
	private EntityModel origin;
	private EntityModel destination;
	private RelationType type;

	// used by Jackson
	public EntityRelation(){}
	
	public EntityRelation(EntityModel origin, EntityModel destination, RelationType type) {
		super();
		this.origin = origin;
		this.destination = destination;
		this.type = type;
	}

	public EntityModel getDestination() {
		return destination;
	}
	
	public EntityModel getOrigin() {
		return origin;
	}
	
	public RelationType getType() {
		return type;
	}

	public static enum RelationType{
		AGGREGATION, COMPOSITION, INHERITANCE
	}
}
