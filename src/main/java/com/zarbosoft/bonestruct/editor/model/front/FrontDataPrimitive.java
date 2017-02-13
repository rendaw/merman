package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.grammar.Node;
import org.pcollections.HashTreePSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Luxem.Configuration(name = "primitive")
public class FrontDataPrimitive extends FrontPart {

	@Luxem.Configuration
	public String middle;

	private DataPrimitive dataType;

	@Luxem.Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return new PrimitiveVisualNode(
				context,
				dataType.get(data),
				HashTreePSet
						.from(tags)
						.plus(new VisualNode.PartTag("primitive"))
						.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
		);
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		this.dataType = nodeType.getDataPrimitive(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public String middle() {
		return middle;
	}
}
