package org.reflections;

public final class NullObject {
	public static final NullObject NULL = new NullObject();

	private NullObject() {
	}

	@Override
	public String toString() {
		return null;
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		// NullObject other = (NullObject) obj;
		return true;
	}
}
