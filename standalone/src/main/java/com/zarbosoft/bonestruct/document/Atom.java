package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.editor.visual.tags.TypeTag;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.syntax.AtomType;
import org.pcollections.PSet;

import java.util.Map;

public class Atom {
	public Value.Parent parent;
	public AtomType type;
	public final Map<String, Value> data;
	public VisualAtom visual;
	public PSet<Tag> tags;

	public Atom(final AtomType type, final Map<String, Value> data) {
		this.type = type;
		this.data = data;
		tags = Context.asFreeTags(type.tags).plus(new TypeTag(type.id()));
		data.forEach((k, v) -> {
			v.setParent(new Parent() {
				@Override
				public Atom atom() {
					return Atom.this;
				}

				@Override
				public boolean selectUp(final Context context) {
					if (parent == null)
						return false;
					return Atom.this.parent.selectUp(context);
				}
			});
		});
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
			this.visual = new VisualAtom(context, parent, this, alignments, depth);
		}
		return visual;
	}

	public void setParent(final Value.Parent parent) {
		this.parent = parent;
	}

	public static abstract class Parent {
		public abstract Atom atom();

		public abstract boolean selectUp(final Context context);
	}

}
