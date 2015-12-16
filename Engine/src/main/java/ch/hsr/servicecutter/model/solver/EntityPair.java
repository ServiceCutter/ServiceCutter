package ch.hsr.servicecutter.model.solver;

import ch.hsr.servicecutter.model.usersystem.Nanoentity;

public class EntityPair {
	public final Nanoentity nanoentityA;
	public final Nanoentity nanoentityB;

	public EntityPair(final Nanoentity nanoentityA, final Nanoentity nanoentityB) {
		this.nanoentityA = nanoentityA;
		this.nanoentityB = nanoentityB;
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
