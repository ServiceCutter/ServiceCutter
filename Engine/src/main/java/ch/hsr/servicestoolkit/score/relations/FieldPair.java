package ch.hsr.servicestoolkit.score.relations;

import ch.hsr.servicestoolkit.model.DataField;

public class FieldPair {
	public final DataField fieldA;
	public final DataField fieldB;

	public FieldPair(final DataField fieldA, final DataField fieldB) {
		this.fieldA = fieldA;
		this.fieldB = fieldB;
	}

	@Override
	public int hashCode() {
		return getCompareString().hashCode();
	}

	private String getCompareString() {
		Long idA = fieldA.getId();
		Long idB = fieldB.getId();
		String stringToHash;
		if (idA < idB) {
			stringToHash = idA + "-" + idB;
		} else {
			stringToHash = idB + "-" + idA;
		}
		return stringToHash;
	}

	@Override
	public String toString() {
		return fieldA.getContextName() + " - " + fieldB.getContextName();
	}

	@Override
	public boolean equals(final Object obj) {
		return getCompareString().equals(((FieldPair) obj).getCompareString());
	}

}
