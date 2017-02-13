package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.front.*;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public class PrefixGapNodeType extends NodeType {
	@Luxem.Configuration(name = "prefix", optional = true)
	public List<FrontConstantPart> frontPrefix;
	@Luxem.Configuration(name = "infix", optional = true)
	public List<FrontConstantPart> frontInfix;
	@Luxem.Configuration(name = "suffix", optional = true)
	public List<FrontConstantPart> frontSuffix;

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, DataElement> middle;

	public PrefixGapNodeType() {
		{
			final FrontGapBase gap = new FrontGapBase() {

				@Override
				protected void buildChoices(
						final Context context, final Node self, final Map<String, List<Choice>> types
				) {
					for (final FreeNodeType type : (
							self.parent == null ?
									context.syntax.getLeafTypes(context.syntax.root) :
									context.syntax.getLeafTypes(self.parent.childType())
					)) {
						for (final FreeNodeType.GapKey key : type.gapKeys()) {
							if (key.indexAfter == -1)
								continue;
							if (!key.nodeAfter)
								continue;
							types.putIfAbsent(key.key, new ArrayList<>());
							types.get(key.key).add(new Choice(type, key.indexAfter, -1));
						}
					}
				}

				@Override
				protected void choose(
						final Context context, final Node self, final Choice choice, final String remainder
				) {
					final Node replacement = choice.type.create();
					final DataNode.Value value = (DataNode.Value) self.data.get("value");
					self.parent.replace(context, replacement);
					self.type.front().get(choice.node).dispatch(new NodeDispatchHandler() {
						@Override
						public void handle(final FrontDataArray front) {
							context.history.apply(context,
									new DataArrayBase.ChangeAdd((DataArrayBase.Value) self.data.get(front.middle),
											0,
											ImmutableList.of(value.get())
									)
							);
						}

						@Override
						public void handle(final FrontDataNode front) {
							context.history.apply(context,
									new DataNode.ChangeSet((DataNode.Value) self.data.get(front.middle), value.get())
							);
						}
					});
					replacement.getVisual().select(context);
				}
			};
			final FrontDataNode value = new FrontDataNode();
			value.middle = "value";
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix,
					ImmutableList.of(gap),
					frontInfix,
					ImmutableList.of(value),
					frontSuffix
			));
		}
		{
			final BackType type = new BackType();
			type.value = "__gap";
			final BackDataPrimitive gap = new BackDataPrimitive();
			gap.middle = "gap";
			final BackDataNode value = new BackDataNode();
			value.middle = "value";
			final BackRecord record = new BackRecord();
			record.pairs.put("gap", gap);
			record.pairs.put("value", value);
			back = ImmutableList.of(type, record);
		}
		{
			final DataNode value = new DataNode();
			value.id = "value";
			final DataPrimitive gap = new DataPrimitive();
			gap.id = "gap";
			middle = ImmutableMap.of("gap", gap, "value", value);
		}
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, DataElement> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	protected Map<String, AlignmentDefinition> alignments() {
		return ImmutableMap.of();
	}

	@Override
	public int precedence() {
		return 1_000_000;
	}

	@Override
	public boolean frontAssociative() {
		return false;
	}

	@Override
	public void finish(final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {

	}

	@Override
	public String name() {
		return "Gap (prefix)";
	}
}
