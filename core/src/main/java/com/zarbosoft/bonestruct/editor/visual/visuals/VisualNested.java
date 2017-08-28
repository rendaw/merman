package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.syntax.middle.MiddleAtom;
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
			final int depth
	) {
		super(tags);
		this.value = value;
		dataListener = new ValueAtom.Listener() {
			@Override
			public void set(final Context context, final Atom atom) {
				VisualNested.this.set(context, atom);
			}
		};
		value.addListener(dataListener);
		value.visual = this;
		root(context, parent, alignments, depth);
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
