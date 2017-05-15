package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtomType;
import com.zarbosoft.bonestruct.syntax.AtomType;

import java.util.Map;

public class Atom extends DocumentNode {
	public Value.Parent parent;
	public AtomType type;
	public final Map<String, Value> data;
	public VisualAtomType visual;

	public Atom(final AtomType type, final Map<String, Value> data) {
		this.type = type;
		this.data = data;
		data.forEach((k, v) -> {
			v.setParent(new Parent() {
				@Override
				public Atom atom() {
					return Atom.this;
				}

				@Override
				public void selectUp(final Context context) {
					Atom.this.parent.selectUp(context);
				}
			});
		});
	}

	/**
	 * Only for document root.
	 *
	 * @param data
	 */
	public Atom(final Map<String, Value> data) {
		this.type = null;
		this.data = data;
	}

	public Path getPath() {
		if (parent == null)
			return new Path();
		else
			return parent.path();
	}

	public Visual createVisual(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		if (visual != null) {
			visual.root(context, parent, alignments, depth);
		} else {
			this.visual = type.createVisual(context, parent, this, alignments, depth);
		}
		return visual;
	}

	public void setParent(final Value.Parent parent) {
		this.parent = parent;
	}

	@Override
	public DocumentNodeParent parent() {
		return parent;
	}

	@Override
	public VisualAtomType visual() {
		return visual;
	}

	public static abstract class Parent extends DocumentNodeParent {
		public abstract Atom atom();
	}

}
