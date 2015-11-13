package ch.hsr.servicestoolkit.score.relations;

import ch.hsr.servicestoolkit.model.DataField;

public class FieldTuple {
	public final DataField fieldA;
	public final DataField fieldB;

	public FieldTuple(final DataField fieldA, final DataField fieldB) {
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
		return fieldA.getName() + " - " + fieldB.getName();
	}

	@Override
	public boolean equals(final Object obj) {
		return getCompareString().equals(((FieldTuple) obj).getCompareString());
	}

}
