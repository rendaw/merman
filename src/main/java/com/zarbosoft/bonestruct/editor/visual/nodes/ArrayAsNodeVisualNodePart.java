package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.history.changes.ChangeArrayAdd;
import com.zarbosoft.bonestruct.history.changes.ChangeArrayRemove;

import java.util.List;
import java.util.Set;

public class ArrayAsNodeVisualNodePart extends NodeVisualNodePartBase {
	final ValueArray data;
	private final ValueArray.Listener dataListener;

	public ArrayAsNodeVisualNodePart(
			final Context context, final ValueArray data, final Set<Tag> tags
	) {
		super(tags);
		this.data = data;
		dataListener = new ValueArray.Listener() {
			@Override
			public void added(final Context context, final int index, final List<Node> nodes) {
				set(context, nodes.get(0));
			}

			@Override
			public void removed(final Context context, final int index, final int count) {

			}
		};
		data.addListener(dataListener);
		set(context, data.get().get(0));
		data.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node value) {
		context.history.apply(context, new ChangeArrayRemove(data, 0, 1));
		context.history.apply(context, new ChangeArrayAdd(data, 0, ImmutableList.of(value)));
	}

	@Override
	public void destroy(final Context context) {
		data.removeListener(dataListener);
		data.visual = null;
		super.destroy(context);
	}
}
