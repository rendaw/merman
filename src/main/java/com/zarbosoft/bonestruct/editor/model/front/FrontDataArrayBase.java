package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.pidgoon.Node;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.ArrayVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.HashTreePSet;

import java.util.*;
import java.util.stream.Collectors;

public abstract class FrontDataArrayBase extends FrontPart {

	@Configuration
	public List<FrontConstantPart> prefix = new ArrayList<>();
	@Configuration
	public List<FrontConstantPart> suffix = new ArrayList<>();
	@Configuration
	public List<FrontConstantPart> separator = new ArrayList<>();
	protected DataArrayBase dataType;
	@Configuration(optional = true)
	public Map<String, Node> hotkeys = new HashMap<>();
	@Configuration(name = "tag-first", optional = true)
	public boolean tagFirst = false;
	@Configuration(name = "tag-last", optional = true)
	public boolean tagLast = false;

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
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return new ArrayVisualNode(
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
			protected Map<String, Node> getHotkeys() {
				return hotkeys;
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
