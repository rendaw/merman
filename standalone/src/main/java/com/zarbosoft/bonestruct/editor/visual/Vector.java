package com.zarbosoft.bonestruct.editor.visual;

public class Vector {
	public final int converse;
	public final int transverse;

	public Vector(final int converse, final int transverse) {
		this.converse = converse;
		this.transverse = transverse;
	}

	public Vector add(final Vector other) {
		return new Vector(converse + other.converse, transverse + other.transverse);
	}

	public Vector setTransverse(final int transverse) {
		return new Vector(converse, transverse);
	}

	public Vector addTransverse(final int transverse) {
		return new Vector(converse, this.transverse + transverse);
	}

	public Vector setConverse(final int converse) {
		return new Vector(converse, transverse);
	}

	public Vector addConverse(final int converse) {
		return new Vector(this.converse + converse, transverse);
	}

	public String toString() {
		return String.format("<%d,%d>", converse, transverse);
	}

	public boolean lessThan(final Vector other) {
		return transverse < other.transverse || (transverse == other.transverse && converse < other.converse);
	}

	public boolean greaterThan(final Vector other) {
		return transverse > other.transverse || (transverse == other.transverse && converse > other.converse);
	}
}
