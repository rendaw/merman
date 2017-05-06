package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePrimitive;
import com.zarbosoft.bonestruct.syntax.modules.hotkeys.grammar.Node;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration(name = "primitive")
public class FrontDataPrimitive extends FrontPart {

	@Configuration
	public String middle;

	private MiddlePrimitive dataType;

	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();

	@Override
	public VisualPart createVisual(
			final Context context, final com.zarbosoft.bonestruct.document.Node node, final PSet<Visual.Tag> tags
	) {
		return new VisualPrimitive(
				context,
				dataType.get(node.data),
				HashTreePSet
						.from(tags)
						.plus(new Visual.PartTag("primitive"))
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
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
