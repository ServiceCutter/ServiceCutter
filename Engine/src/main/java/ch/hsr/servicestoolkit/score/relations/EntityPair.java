package ch.hsr.servicestoolkit.score.relations;

import ch.hsr.servicestoolkit.model.NanoEntity;

public class EntityPair {
	public final NanoEntity nanoentityA;
	public final NanoEntity nanoentityB;

	public EntityPair(final NanoEntity fieldA, final NanoEntity fieldB) {
		this.nanoentityA = fieldA;
		this.nanoentityB = fieldB;
	}

	@Override
	public int hashCode() {
		return getCompareString().hashCode();
	}

	private String getCompareString() {
		Long idA = nanoentityA.getId();
		Long idB = nanoentityB.getId();
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
		return nanoentityA.getContextName() + " - " + nanoentityB.getContextName();
	}

	@Override
	public boolean equals(final Object obj) {
		return getCompareString().equals(((EntityPair) obj).getCompareString());
	}

}
