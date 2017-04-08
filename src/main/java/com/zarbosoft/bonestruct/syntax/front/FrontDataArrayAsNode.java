package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.ArrayAsNodeVisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.hid.grammar.Node;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FrontDataArrayAsNode extends FrontPart {

	@Override
	public String middle() {
		return middle;
	}

	@Configuration
	public String middle;
	private MiddleArray dataType;

	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, Value> data, final Set<VisualNode.Tag> tags
	) {
		return new ArrayAsNodeVisualNodePart(
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
		dataType = (MiddleArray) nodeType.getDataArray(middle);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
	}
}
