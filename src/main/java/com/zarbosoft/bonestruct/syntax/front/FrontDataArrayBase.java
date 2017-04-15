package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualArray;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.hid.grammar.Node;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArrayBase;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FrontDataArrayBase extends FrontPart {

	@Configuration(optional = true)
	public List<FrontConstantPart> prefix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontConstantPart> suffix = new ArrayList<>();
	@Configuration(optional = true)
	public List<FrontConstantPart> separator = new ArrayList<>();
	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();
	@Configuration(name = "tag_first", optional = true)
	public boolean tagFirst = false;
	@Configuration(name = "tag_last", optional = true)
	public boolean tagLast = false;

	protected MiddleArrayBase dataType;

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle());
		dataType = nodeType.getDataArray(middle());
	}

	public abstract String middle();

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, Value> data, final Set<VisualNode.Tag> tags
	) {
		return new VisualArray(
				context,
				dataType.get(data),
				HashTreePSet
						.from(tags)
						.plus(new VisualNode.PartTag("array"))
						.plusAll(this.tags.stream().map(s -> new VisualNode.FreeTag(s)).collect(Collectors.toSet()))
		) {

			@Override
			protected boolean tagLast() {
				return tagLast;
			}

			@Override
			protected boolean tagFirst() {
				return tagFirst;
			}

			@Override
			protected List<FrontConstantPart> getPrefix() {
				return prefix;
			}

			@Override
			protected List<FrontConstantPart> getSuffix() {
				return suffix;
			}

			@Override
			protected List<FrontConstantPart> getSeparator() {
				return separator;
			}
		};
	}
}
