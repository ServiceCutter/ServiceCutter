package ch.hsr.servicestoolkit.model;

public enum CriterionType {
	SAME_ENTITIY, AGGREGATED_ENTITY, COMPOSITION_ENTITY;

	// @Override
	// public String toString() {
	// return this.name();
	// }
	//
	// @JsonCreator
	// public static CriterionType create(String value) {
	// if (value == null) {
	// throw new IllegalArgumentException();
	// }
	// for (CriterionType type : values()) {
	// if (value.equals(type.name())) {
	// return type;
	// }
	// }
	// throw new IllegalArgumentException();
	// }
}
