package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Hoverable;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualParent;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualGroup;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.*;
import com.zarbosoft.bonestruct.wall.Brick;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;

import static com.zarbosoft.rendaw.common.Common.enumerate;
import static com.zarbosoft.rendaw.common.Common.stream;

@Configuration
public abstract class NodeType {
	@Configuration
	public String id;

	public abstract List<FrontPart> front();

	public abstract Map<String, MiddleElement> middle();

	public abstract List<BackPart> back();

	protected abstract Map<String, AlignmentDefinition> alignments();

	public abstract int precedence();

	public abstract boolean frontAssociative();

	public static class NodeBackParent extends BackPart.Parent {
		public int index;

		public NodeBackParent(final int index) {
			this.index = index;
		}
	}

	public void finish(final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {
		middle().forEach((k, v) -> {
			v.id = k;
			v.finish(allTypes, scalarTypes);
		});
		{
			final Set<String> middleUsedBack = new HashSet<>();
			enumerate(back().stream()).forEach(pair -> {
				final Integer i = pair.first;
				final BackPart p = pair.second;
				p.finish(syntax, this, middleUsedBack);
				p.parent = new NodeBackParent(i);
			});
			final Set<String> missing = Sets.difference(middle().keySet(), middleUsedBack);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Middle elements %s in %s are unused by back parts.",
						missing,
						id
				));
		}
		{
			final Set<String> middleUsedFront = new HashSet<>();
			front().forEach(p -> p.finish(this, middleUsedFront));
			final Set<String> missing = Sets.difference(middle().keySet(), middleUsedFront);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Middle elements %s in %s are unused by front parts.",
						missing,
						id
				));
		}
	}

	public com.zarbosoft.pidgoon.Node buildBackRule(final Syntax syntax) {
		final Sequence seq = new Sequence();
		seq.add(new Operator((store) -> store.pushStack(0)));
		back().forEach(p -> seq.add(p.buildBackRule(syntax, this)));
		return new Operator(seq, store -> {
			final Map<String, Value> data = new HashMap<>();
			store = (Store) Helper.<Pair<String, Value>>stackPopSingleList(store,
					pair -> data.put(pair.first, pair.second)
			);
			final Node node = new Node(this, data);
			return store.pushStack(node);
		});
	}

	public NodeTypeVisual createVisual(final Context context, final Node node) {
		return new NodeTypeVisual(context, node);
	}

	public abstract String name();

	public BackPart getBackPart(final String id) {
		final Deque<Iterator<BackPart>> stack = new ArrayDeque<>();
		stack.addLast(back().iterator());
		while (!stack.isEmpty()) {
			final Iterator<BackPart> iterator = stack.peekLast();
			if (!iterator.hasNext())
				continue;
			stack.addLast(iterator);
			final BackPart next = iterator.next();
			if (next instanceof BackArray) {
				stack.addLast(((BackArray) next).elements.iterator());
			} else if (next instanceof BackRecord) {
				stack.addLast(((BackRecord) next).pairs.values().iterator());
			} else if (next instanceof BackDataArray) {
				if (((BackDataArray) next).middle.equals(id))
					return next;
			} else if (next instanceof BackDataKey) {
				if (((BackDataKey) next).middle.equals(id))
					return next;
			} else if (next instanceof BackDataNode) {
				if (((BackDataNode) next).middle.equals(id))
					return next;
			} else if (next instanceof BackDataPrimitive) {
				if (((BackDataPrimitive) next).middle.equals(id))
					return next;
			} else if (next instanceof BackDataRecord) {
				if (((BackDataRecord) next).middle.equals(id))
					return next;
			}
		}
		throw new DeadCode();
	}

	public MiddleRecord getDataRecord(final String middle) {
		return getData(MiddleRecord.class, middle);
	}

	public class NodeTypeVisual extends Visual {
		private final VisualGroup body;
		private final Node node;
		private boolean compact;
		private VisualParent parent;
		public Map<String, VisualPart> frontToData = new HashMap<>();

		public NodeTypeVisual(final Context context, final Node node) {
			super(HashTreePSet.<Tag>empty().plus(new TypeTag(id)).plus(new PartTag("node")));
			this.node = node;
			final PSet<Tag> tags = HashTreePSet.singleton(new TypeTag(id));
			compact = false;
			body = new VisualGroup(ImmutableSet.of());
			for (final Map.Entry<String, AlignmentDefinition> entry : alignments().entrySet()) {
				body.alignments.put(entry.getKey(), entry.getValue().create());
			}
			enumerate(stream(front())).forEach(pair -> {
				final VisualPart visual = pair.second.createVisual(context, node.data, tags);
				frontToData.put(pair.second.middle(), visual);
				body.add(context, visual);
			});
			body.setParent(new VisualParent() {

				@Override
				public void selectUp(final Context context) {
					parent.selectUp(context);
				}

				@Override
				public Brick createNextBrick(final Context context) {
					return parent.createNextBrick(context);
				}

				@Override
				public Brick createPreviousBrick(final Context context) {
					return parent.createPreviousBrick(context);
				}

				@Override
				public Visual getTarget() {
					return parent.getTarget();
				}

				@Override
				public NodeType.NodeTypeVisual getNodeVisual() {
					return NodeTypeVisual.this;
				}

				@Override
				public Alignment getAlignment(final String alignment) {
					return parent.getAlignment(alignment);
				}

				@Override
				public Brick getPreviousBrick(final Context context) {
					if (parent == null)
						return null;
					return parent.getPreviousBrick(context);
				}

				@Override
				public Brick getNextBrick(final Context context) {
					if (parent == null)
						return null;
					return parent.getNextBrick(context);
				}

				@Override
				public Hoverable hover(
						final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector point
				) {
					if (parent == null)
						return null;
					return parent.hover(context, point);
				}
			});
			node.visual = this;
		}

		@Override
		public void setParent(final VisualParent parent) {
			this.parent = parent;
		}

		@Override
		public VisualParent parent() {
			return body.parent();
		}

		@Override
		public boolean selectDown(final Context context) {
			return body.selectDown(context);
		}

		@Override
		public void select(final Context context) {
			throw new DeadCode();
		}

		@Override
		public void selectUp(final Context context) {
			if (parent == null)
				return;
			parent.selectUp(context);
		}

		@Override
		public Brick createFirstBrick(final Context context) {
			return body.createFirstBrick(context);
		}

		@Override
		public Brick createLastBrick(final Context context) {
			return body.createLastBrick(context);
		}

		@Override
		public Brick getFirstBrick(final Context context) {
			return body.getFirstBrick(context);
		}

		@Override
		public Brick getLastBrick(final Context context) {
			return body.getLastBrick(context);
		}

		@Override
		public int spacePriority() {
			return -precedence();
		}

		@Override
		public boolean canCompact() {
			return !compact;
		}

		@Override
		public void compact(final Context context) {
			body.compact(context);
			compact = true;
		}

		@Override
		public boolean canExpand() {
			return compact;
		}

		@Override
		public void expand(final Context context) {
			body.expand(context);
			compact = false;
		}

		@Override
		public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
				final Context context, final TagsChange change
		) {
			return body.getPropertiesForTagsChange(context, change);
		}

		@Override
		public void rootAlignments(
				final Context context, final Map<String, Alignment> alignments
		) {
			body.rootAlignments(context, alignments);
		}

		@Override
		public void destroy(final Context context) {
			node.visual = null;
			body.destroy(context);
		}

		@Override
		public boolean isAt(final Value value) {
			return false;
		}

		@Override
		public void tagsChanged(final Context context) {

		}

		public NodeType getType() {
			return NodeType.this;
		}
	}

	private <D extends MiddleElement> D getData(final Class<? extends MiddleElement> type, final String id) {
		final MiddleElement found = middle().get(id);
		if (found == null) {
			throw new InvalidSyntax(String.format("No middle element [%s] in [%s]", id, this.id));
		} else {
			if (!type.isAssignableFrom(found.getClass()))
				throw new InvalidSyntax(String.format("Conflicting types for middle element [%s] in [%s]: %s, %s",
						id,
						this.id,
						found.getClass(),
						type
				));
		}
		return (D) found;
	}

	public MiddlePrimitive getDataPrimitive(final String key) {
		return getData(MiddlePrimitive.class, key);
	}

	public MiddleNode getDataNode(final String key) {
		return getData(MiddleNode.class, key);
	}

	public MiddleArrayBase getDataArray(final String key) {
		return getData(MiddleArrayBase.class, key);
	}

	public MiddleRecordKey getDataRecordKey(final String key) {
		return getData(MiddleRecordKey.class, key);
	}
}
