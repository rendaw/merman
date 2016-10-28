package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataNode;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.parts.NestedVisualNodePart;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "node")
public class FrontDataNode implements FrontPart {

	@Luxem.Configuration
	public String middle;
	private DataNode dataType;

	@Luxem.Configuration(name = "style", optional = true)
	public FrontMark.Style style = new FrontMark.Style();
	@Luxem.Configuration(optional = true)
	public Map<String, com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node> hotkeys = new HashMap<>();

	@Override
	public VisualNodePart createVisual(final Context context, final Map<String, Object> data) {
		return new NestedVisualNodePart(dataType.get(data).createVisual(context)) {
			@Override
			public Map<String, Node> getHotkeys(final Context contex) {
				return hotkeys;
			}

			@Override
			public Break breakMode() {
				return style.breakMode;
			}

			@Override
			public String alignmentName() {
				return style.alignment;
			}

			@Override
			public String alignmentNameCompact() {
				return style.alignmentCompact;
			}
		};
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataNode(middle);
	}
}
