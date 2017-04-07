package com.zarbosoft.bonestruct.editor.visual.nodes;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.List;
import java.util.Set;

public class ArrayAsNodeVisualNodePart extends NodeVisualNodePartBase {
	final DataArray.Value data;

	public ArrayAsNodeVisualNodePart(
			final Context context, final DataArray.Value data, final Set<Tag> tags
	) {
		super(tags);
		this.data = data;
		data.addListener(new DataArray.Listener() {
			@Override
			public void added(final Context context, final int index, final List<Node> nodes) {
				set(context, nodes.get(0));
			}

			@Override
			public void removed(final Context context, final int index, final int count) {

			}
		});
		set(context, data.get().get(0));
		data.visual = this;
	}

	@Override
	protected void nodeSet(final Context context, final Node value) {
		context.history.apply(context, new DataArray.ChangeRemove(data, 0, 1));
		context.history.apply(context, new DataArray.ChangeAdd(data, 0, ImmutableList.of(value)));
	}

	@Override
	public void destroy(final Context context) {
		data.visual = null;
		super.destroy(context);
	}
}
