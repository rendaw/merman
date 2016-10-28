package com.zarbosoft.bonestruct.model.front;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.nodes.parts.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodePart;
import com.zarbosoft.luxemj.Luxem;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration(name = "primitive")
public class FrontDataPrimitive implements FrontPart {

	@Luxem.Configuration
	public String middle;
	private DataPrimitive dataType;

	@Luxem.Configuration
	public static class PrimitiveStyle extends FrontMark.Style {
		@Luxem.Configuration(optional = true)
		public boolean block = false;

		@Luxem.Configuration(name = "soft-indent", optional = true)
		public int softIndent = 0;

		@Luxem.Configuration(optional = true)
		public boolean level = true;
	}

	@Luxem.Configuration(optional = true)
	public PrimitiveStyle style = new PrimitiveStyle();
	@Luxem.Configuration(optional = true)
	public Map<String, com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node> hotkeys = new HashMap<>();

	@Override
	public VisualNodePart createVisual(final Context context, final Map<String, Object> data) {
		return new PrimitiveVisualNode(context, dataType.get(data)) {

			@Override
			protected int softIndent() {
				return style.softIndent;
			}

			@Override
			protected boolean breakFirst() {
				return style.block;
			}

			@Override
			protected boolean level() {
				return style.level;
			}

			@Override
			protected String alignment() {
				return null;
			}
		};
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		this.dataType = nodeType.getDataPrimitive(middle);
	}
}
