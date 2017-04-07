package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.bonestruct.editor.model.middle.DataPrimitive;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.pidgoon.ParseContext;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.bytes.Parse;
import com.zarbosoft.pidgoon.bytes.Position;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Wildcard;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.iterable;

public abstract class FrontGapBase extends FrontPart {
	private DataPrimitive dataType;

	@Override
	public VisualNodePart createVisual(
			final Context context, final Map<String, DataElement.Value> data, final Set<VisualNode.Tag> tags
	) {
		return new GapPrimitiveVisualNode(context, data, tags);
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle());
		this.dataType = nodeType.getDataPrimitive(middle());
	}

	protected abstract void process(
			final Context context, final Node self, final String string, final Common.UserData store
	);

	private class GapPrimitiveVisualNode extends PrimitiveVisualNode {
		private final Map<String, DataElement.Value> data;

		public GapPrimitiveVisualNode(
				final Context context, final Map<String, DataElement.Value> data, final Set<Tag> tags
		) {
			super(context,
					FrontGapBase.this.dataType.get(data),
					HashTreePSet
							.from(tags)
							.plus(new PartTag("primitive"))
							.plusAll(FrontGapBase.this.tags
									.stream()
									.map(s -> new FreeTag(s))
									.collect(Collectors.toSet()))
			);
			this.data = data;
		}

		@Override
		public PrimitiveSelection createSelection(
				final Context context, final int beginOffset, final int endOffset, final boolean direct
		) {
			return new GapSelection(context, beginOffset, endOffset, direct);
		}

		public class GapSelection extends PrimitiveSelection {

			private final DataPrimitive.Value self;
			private final Common.UserData userData = new Common.UserData();

			public GapSelection(
					final Context context, final int beginOffset, final int endOffset, final boolean direct
			) {
				super(context, beginOffset, endOffset, direct);
				self = dataType.get(data);
			}

			@Override
			public void receiveText(final Context context, final String text) {
				super.receiveText(context, text);
				process(context, self.parent.node(), self.get(), userData);
			}

			@Override
			public void clear(final Context context) {
				super.clear(context);
				deselect(context, self.parent.node(), self.get(), userData);
			}
		}
	}

	protected abstract void deselect(Context context, Node self, String string, Common.UserData userData);

	public DataPrimitive.Value findSelectNext(
			final com.zarbosoft.bonestruct.editor.model.Node node, boolean skipFirstNode
	) {
		for (final FrontPart front : node.type.front()) {
			if (front instanceof FrontDataPrimitive) {
				return (DataPrimitive.Value) node.data(((FrontDataPrimitive) front).middle);
			} else if (front instanceof FrontGapBase) {
				return (DataPrimitive.Value) node.data(middle());
			} else if (front instanceof FrontDataNode) {
				if (skipFirstNode) {
					skipFirstNode = false;
				} else {
					final DataPrimitive.Value found =
							findSelectNext(((DataNode.Value) node.data(((FrontDataNode) front).middle)).get(),
									skipFirstNode
							);
					if (found != null)
						return found;
				}
			} else if (front instanceof FrontDataArray) {
				final DataArrayBase.Value array = (DataArrayBase.Value) node.data(((FrontDataArray) front).middle);
				for (final Node element : array.get()) {
					if (skipFirstNode) {
						skipFirstNode = false;
					} else {
						final DataPrimitive.Value found = findSelectNext(element, skipFirstNode);
						if (found != null)
							return found;
					}
				}
			}
		}
		return null;
	}

	protected void select(final Context context, final DataPrimitive.Value value) {
		value.parent.node().getVisual().frontToData.get(value.data.id).select(context);
	}

	@Override
	public void dispatch(final DispatchHandler handler) {
		handler.handle(this);
	}

	@Override
	public String middle() {
		return "gap";
	}

	protected static class GapKey {
		public int indexBefore;
		public List<FrontPart> keyParts = new ArrayList<>();
		public int indexAfter;

		public com.zarbosoft.pidgoon.Node matchGrammar(final FreeNodeType type) {
			final Sequence out = new Sequence();
			for (final FrontPart part : keyParts) {
				if (part instanceof FrontImage) {
					out.add(Grammar.stringSequence(((FrontImage) part).gapKey));
				} else if (part instanceof FrontMark) {
					out.add(Grammar.stringSequence(((FrontMark) part).value));
				} else if (part instanceof FrontDataPrimitive) {
					final DataPrimitive middle = (DataPrimitive) type.middle.get(((FrontDataPrimitive) part).middle);
					out.add(middle.validation == null ? new Repeat(new Wildcard()) : middle.validation.build());
				} else
					throw new DeadCode();
			}
			return out;
		}

		public class ParseResult {
			public Node node;
			public FrontPart nextInput;
			public String remainder;
		}

		public ParseResult parse(final Context context, final FreeNodeType type, final String string) {
			final ParseResult out = new ParseResult();
			final Iterator<FrontPart> frontIterator = keyParts.iterator();

			// Parse string into primitive parts
			final Set<String> filled = new HashSet<>();
			filled.addAll(type.middle.keySet());
			final Map<String, DataElement.Value> data = new HashMap<>();
			int at = 0;
			for (final FrontPart front : iterable(frontIterator)) {
				final Grammar grammar = new Grammar();
				if (front instanceof FrontSpace)
					continue;
				else if (front instanceof FrontImage)
					grammar.add("root", Grammar.stringSequence(((FrontImage) front).gapKey));
				else if (front instanceof FrontMark)
					grammar.add("root", Grammar.stringSequence(((FrontMark) front).value));
				else if (front instanceof FrontDataPrimitive) {
					final DataPrimitive middle = (DataPrimitive) type.middle.get(((FrontDataPrimitive) front).middle);
					grammar.add("root",
							middle.validation == null ? new Repeat(new Wildcard()) : middle.validation.build()
					);
				} else
					throw new DeadCode();
				final Pair<ParseContext, Position> longest = new Parse<>()
						.grammar(grammar)
						.longestMatchFromStart(new ByteArrayInputStream(string
								.substring(at)
								.getBytes(StandardCharsets.UTF_8)));
				if (longest.second.distance() == 0)
					throw new AssertionError();
				if (front instanceof FrontDataPrimitive) {
					data.put(front.middle(), new DataPrimitive.Value(type.getDataPrimitive(front.middle()),
							string.substring(at, at + (int) longest.second.distance())
					));
					filled.remove(front.middle());
				}
				at = at + (int) longest.second.distance();
				if (at >= string.length())
					break;
			}
			filled.forEach(middle -> data.put(middle, type.middle.get(middle).create(context.syntax)));
			out.remainder = string.substring(at);
			out.node = new Node(type, data);

			// Look for the next place to enter text
			for (final FrontPart part : iterable(frontIterator)) {
				if (part instanceof FrontDataPrimitive) {
					out.nextInput = part;
					break;
				}
			}

			return out;
		}
	}

	protected static List<GapKey> gapKeys(final FreeNodeType type) {
		final List<GapKey> out = new ArrayList<>();
		final Common.Mutable<GapKey> top = new Common.Mutable<>(new GapKey());
		top.value.indexBefore = -1;
		enumerate(type.front().stream()).forEach(p -> {
			p.second.dispatch(new FrontPart.DispatchHandler() {
				private void flush(final boolean node) {
					if (!top.value.keyParts.isEmpty()) {
						top.value.indexAfter = p.first;
						out.add(top.value);
						top.value = new GapKey();
					}
					top.value.indexBefore = p.first;
				}

				@Override
				public void handle(final FrontSpace front) {

				}

				@Override
				public void handle(final FrontImage front) {
					top.value.keyParts.add(front);
				}

				@Override
				public void handle(final FrontMark front) {
					top.value.keyParts.add(front);
				}

				@Override
				public void handle(final FrontDataArrayBase front) {
					front.prefix.forEach(front2 -> front2.dispatch(this));
					flush(true);
					front.suffix.forEach(front2 -> front2.dispatch(this));
				}

				@Override
				public void handle(final FrontDataNode front) {
					flush(true);
				}

				@Override
				public void handle(final FrontDataPrimitive front) {
					top.value.keyParts.add(front);
				}

				@Override
				public void handle(final FrontGapBase front) {
					throw new DeadCode();
				}
			});
		});
		if (!top.value.keyParts.isEmpty()) {
			top.value.indexAfter = -1;
			out.add(top.value);
		}
		return out;
	}
}
