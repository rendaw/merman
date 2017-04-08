package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.Set;

public class NodeVisualNodePart extends NodeVisualNodePartBase {
	final DataNode.Value data;
	final private DataNode.Listener dataListener;

	public NodeVisualNodePart(
			final Context context, final DataNode.Value data, final Set<Tag> tags
	) {
		super(tags);
		this.data = data;
		dataListener = new DataNode.Listener() {
			@Override
			public void set(final Context context, final Node node) {
				NodeVisualNodePart.this.set(context, node);
			}
		};
		data.addListener(dataListener);
		set(context, data.get());
		data.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node value) {
		context.history.apply(context, new DataNode.ChangeSet(data, context.syntax.gap.create()));
	}

	@Override
	public void destroy(final Context context) {
		data.removeListener(dataListener);
		data.visual = null;
		super.destroy(context);
	}
}
