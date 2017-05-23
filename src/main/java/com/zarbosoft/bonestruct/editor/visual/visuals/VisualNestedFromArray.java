package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.editor.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.tags.Tag;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;

public abstract class VisualNestedFromArray extends VisualNestedBase {
	final ValueArray value;
	private final ValueArray.Listener dataListener;

	public VisualNestedFromArray(
			final Context context,
			final VisualParent parent,
			final ValueArray value,
			final PSet<Tag> tags,
			final Map<String, Alignment> alignments,
			final int depth
	) {
		super(tags);
		this.value = value;
		dataListener = new ValueArray.Listener() {
			@Override
			public void changed(final Context context, final int index, final int remove, final List<Atom> add) {
				set(context, add.get(0));
			}
		};
		value.addListener(dataListener);
		value.visual = this;
		root(context, parent, alignments, depth);
	}

	@Override
	protected void nodeSet(final Context context, final Atom atom) {
		context.history.apply(context, new ChangeArray(value, 0, 1, ImmutableList.of(atom)));
	}

	@Override
	protected Atom atomGet() {
		if (value.data.isEmpty())
			return null;
		return value.data.get(0);
	}

	@Override
	protected String nodeType() {
		return ((MiddleArrayBase) value.middle()).type;
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
