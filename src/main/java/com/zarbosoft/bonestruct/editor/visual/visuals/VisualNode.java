package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;
import com.zarbosoft.bonestruct.syntax.middle.MiddleNode;
import org.pcollections.PSet;

public abstract class VisualNode extends VisualNodeBase {
	final ValueNode value;
	final private ValueNode.Listener dataListener;

	public VisualNode(
			final Context context, final ValueNode value, final PSet<Tag> tags
	) {
		super(tags);
		this.value = value;
		dataListener = new ValueNode.Listener() {
			@Override
			public void set(final Context context, final Node node) {
				VisualNode.this.set(context, node);
			}
		};
		value.addListener(dataListener);
		coreSet(context, value.get());
		value.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node node) {
		context.history.apply(context, new ChangeNodeSet(this.value, node));
	}

	@Override
	protected Node nodeGet() {
		return value.get();
	}

	@Override
	protected String nodeType() {
		return ((MiddleNode) value.middle()).type;
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
