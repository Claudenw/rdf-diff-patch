package org.xenei.rdf_diff_patch;

public abstract class AbstractSpan implements Span {


	@Override
	public final boolean overlap(Span other) {
		return Span.Util.overlaps(this, other);
	}

	@Override
	public final boolean contains(int pos) {
		return Span.Util.contains(this, pos);
	}

	@Override
	public final String toString() {
		return Span.Util.toString(this);
	}
}