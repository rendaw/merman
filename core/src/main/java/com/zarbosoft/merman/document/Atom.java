package com.zarbosoft.merman.document;

import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TypeTag;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.syntax.AtomType;
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
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int depth,
			final int depthScore
	) {
		if (visual != null) {
			visual.root(context, parent, alignments, depth, depthScore);
		} else {
			this.visual = new VisualAtom(context, parent, this, alignments, depth, depthScore);
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
