package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.history.changes.ChangeArray;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;

import java.util.List;
import java.util.Set;

public abstract class VisualNodeFromArray extends VisualNodeBase {
	final ValueArray value;
	private final ValueArray.Listener dataListener;

	public VisualNodeFromArray(
			final Context context, final ValueArray value, final Set<Tag> tags
	) {
		super(tags);
		this.value = value;
		dataListener = new ValueArray.Listener() {
			@Override
			public void changed(final Context context, final int index, final int remove, final List<Node> add) {
				set(context, add.get(0));
			}
		};
		value.addListener(dataListener);
		coreSet(context, value.get().get(0));
		value.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node node) {
		context.history.apply(context, new ChangeArray(value, 0, 1, ImmutableList.of(node)));
	}

	@Override
	protected Node nodeGet() {
		return value.get().get(0);
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
	public void destroy(final Context context) {
		value.removeListener(dataListener);
		value.visual = null;
		super.destroy(context);
	}

	@Override
	public boolean isAt(final Value value) {
		return this.value == value;
	}

	@Override
	public void tagsChanged(final Context context) {

	}
}
