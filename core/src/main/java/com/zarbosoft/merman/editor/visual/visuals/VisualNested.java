package com.zarbosoft.merman.editor.visual.visuals;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Path;
import com.zarbosoft.merman.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.merman.editor.visual.Alignment;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.VisualParent;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.syntax.middle.MiddleAtom;
import org.pcollections.PSet;

import java.util.Map;

public abstract class VisualNested extends VisualNestedBase {
	final ValueAtom value;
	final private ValueAtom.Listener dataListener;

	public VisualNested(
			final Context context,
			final VisualParent parent,
			final ValueAtom value,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		super(tags, visualDepth);
		this.value = value;
		dataListener = new ValueAtom.Listener() {
			@Override
			public void set(final Context context, final Atom atom) {
				VisualNested.this.set(context, atom);
			}
		};
		value.addListener(dataListener);
		value.visual = this;
		root(context, parent, alignments, visualDepth, depthScore);
	}

	@Override
	protected void nodeSet(final Context context, final Atom atom) {
		context.history.apply(context, new ChangeNodeSet(this.value, atom));
	}

	@Override
	protected Atom atomGet() {
		return value.get();
	}

	@Override
	protected String nodeType() {
		return ((MiddleAtom) value.middle()).type;
	}

	@Override
	protected Value value() {
		return value;
	}

	@Override
	protected Path getSelectionPath() {
		return value.getPath();
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		value.removeListener(dataListener);
		value.visual = null;
		super.uproot(context, root);
	}

	@Override
	public void tagsChanged(final Context context) {

	}

}
