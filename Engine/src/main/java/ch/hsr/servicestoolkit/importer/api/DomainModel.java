package ch.hsr.servicestoolkit.importer.api;

import java.util.List;

public class DomainModel {
	List<EntityModel> entities;
	List<EntityRelation> relations;
	
	public DomainModel(){}
	
	public List<EntityModel> getEntities() {
		return entities;
	}
	public void setEntities(List<EntityModel> entities) {
		this.entities = entities;
	}
	public List<EntityRelation> getRelations() {
		return relations;
	}
	public void setRelations(List<EntityRelation> relations) {
		this.relations = relations;
	}
	
	
}
