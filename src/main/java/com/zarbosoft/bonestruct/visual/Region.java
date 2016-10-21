package com.zarbosoft.bonestruct.visual;

public class Region {
	public int converseStart;
	public int converseEdge;
	public int transverseStart;
	public int transverseEdge;

	public Region(final Vector basis) {
		converseStart = basis.converse;
		converseEdge = basis.converse;
		transverseStart = basis.transverse;
		transverseEdge = basis.transverse;
	}

	public void expand(final Vector offset) {
		converseStart = Math.min(converseStart, offset.converse);
		converseEdge = Math.max(converseEdge, offset.converse);
		transverseStart = Math.min(transverseStart, offset.transverse);
		transverseEdge = Math.max(transverseEdge, offset.transverse);
	}
}
