package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.NestedVisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.grammar.Node;
import org.pcollections.HashTreePSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "node")
public class FrontDataNode extends FrontPart {

	@Luxem.Configuration
	public String middle;
	private DataNode dataType;

	@Luxem.Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return new NestedVisualNodePart(
				context,
				dataType.get(data),
				HashTreePSet
						.from(tags)
						.plus(new VisualNode.PartTag("nested"))
						.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
		);
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataNode(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}
}
