package ch.hsr.servicecutter.importer.api;

import java.util.List;

public class EntityRelationDiagram {
	String name;
	List<Entity> entities;
	List<EntityRelation> relations;

	public EntityRelationDiagram() {
	}

	public List<Entity> getEntities() {
		return entities;
	}

	public void setEntities(final List<Entity> entities) {
		this.entities = entities;
	}

	public List<EntityRelation> getRelations() {
		return relations;
	}

	public void setRelations(final List<EntityRelation> relations) {
		this.relations = relations;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
