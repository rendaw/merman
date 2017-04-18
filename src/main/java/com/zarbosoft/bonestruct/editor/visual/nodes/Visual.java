package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.history.changes.ChangeNodeSet;

import java.util.Set;

public class Visual extends VisualNodeBase {
	final ValueNode data;
	final private ValueNode.Listener dataListener;

	public Visual(
			final Context context, final ValueNode data, final Set<Tag> tags
	) {
		super(tags);
		this.data = data;
		dataListener = new ValueNode.Listener() {
			@Override
			public void set(final Context context, final Node node) {
				Visual.this.set(context, node);
			}
		};
		data.addListener(dataListener);
		set(context, data.get());
		data.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node value) {
		context.history.apply(context, new ChangeNodeSet(data, context.syntax.gap.create()));
	}

	@Override
	public void destroy(final Context context) {
		data.removeListener(dataListener);
		data.visual = null;
		super.destroy(context);
	}

	@Override
	public void tagsChanged(final Context context) {

	}
}
