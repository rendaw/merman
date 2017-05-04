package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeFromArray;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.modules.hotkeys.grammar.Node;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
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
	public VisualPart createVisual(
			final Context context, final com.zarbosoft.bonestruct.document.Node node, final Set<Visual.Tag> tags
	) {
		return new VisualNodeFromArray(
				context,
				dataType.get(node.data),
				HashTreePSet
						.from(tags)
						.plus(new Visual.PartTag("nested"))
						.plusAll(this.tags.stream().map(s -> new Visual.FreeTag(s)).collect(Collectors.toSet()))
		) {

			@Override
			public int ellipsizeThreshold() {
				// Only used for gaps; don't worry about gap ellipsizing
				return Integer.MAX_VALUE;
			}

			@Override
			protected Symbol ellipsis() {
				return null;
			}
		};
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
