package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.back.BackDataPrimitive;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.back.BackType;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.model.front.FrontGapBase;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Luxem.Configuration
public class GapNodeType extends NodeType {
	private final DataPrimitive dataGap;
	@Luxem.Configuration
	public List<FrontConstantPart> frontPrefix = new ArrayList<>();
	@Luxem.Configuration
	public List<FrontConstantPart> frontSuffix = new ArrayList<>();

	private final List<FrontPart> front;
	private final List<BackPart> back;
	private final Map<String, DataElement> middle;

	public GapNodeType() {
		id = "__gap";
		{
			final FrontGapBase gap = new FrontGapBase() {
				@Override
				protected void buildChoices(
						final Context context, final Node self, final Map<String, List<Choice>> choices
				) {
					for (final FreeNodeType type : (
							self.parent == null ?
									context.syntax.getLeafTypes(context.syntax.root.type) :
									context.syntax.getLeafTypes(self.parent.childType())
					)) {
						for (final FreeNodeType.GapKey key : type.gapKeys()) {
							choices.putIfAbsent(key.key, new ArrayList<>());
							choices.get(key.key).add(new Choice(type, -1, key.indexAfter));
						}
					}
				}

				@Override
				protected void choose(
						final Context context, final Node self, final Choice choice, final String remainder
				) {
					final Node node = choice.type.create();
					DataPrimitive.Value selectNext = findSelectNext(node, false);
					final com.zarbosoft.bonestruct.editor.model.Node replacement;
					if (selectNext == null) {
						replacement = context.syntax.suffixGap.create(true, node);
						selectNext = findSelectNext(replacement, false);
					} else {
						replacement = node;
					}
					self.parent.replace(context, replacement);
					select(context, selectNext);
					setRemainder(context, selectNext, remainder);
				}
			};
			front = ImmutableList.copyOf(Iterables.concat(frontPrefix, ImmutableList.of(gap), frontSuffix));
		}
		{
			final BackType backType = new BackType();
			backType.value = "__gap";
			final BackDataPrimitive backDataPrimitive = new BackDataPrimitive();
			backDataPrimitive.middle = "gap";
			back = ImmutableList.of(backType, backDataPrimitive);
		}
		{
			dataGap = new DataPrimitive();
			dataGap.id = "gap";
			middle = ImmutableMap.of("gap", dataGap);
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
	public String name() {
		return "Gap";
	}

	public Node create() {
		return new Node(this, ImmutableMap.of("gap", new DataPrimitive.Value(dataGap, "")));
	}
}
