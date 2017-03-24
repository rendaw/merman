package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.DeadCode;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.back.*;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.Alignment;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.GroupVisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.wall.Brick;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Sequence;
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

	public abstract Map<String, DataElement> middle();

	public abstract List<BackPart> back();

	protected abstract Map<String, AlignmentDefinition> alignments();

	public abstract int precedence();

	public abstract boolean frontAssociative();

	public void finish(final Syntax syntax, final Set<String> allTypes, final Set<String> scalarTypes) {
		middle().forEach((k, v) -> {
			v.id = k;
			v.finish(allTypes, scalarTypes);
		});
		{
			final Set<String> middleUsedBack = new HashSet<>();
			back().forEach(p -> p.finish(syntax, this, middleUsedBack));
			final Set<String> missing = Sets.difference(middle().keySet(), middleUsedBack);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by back parts.",
						this,
						missing
				));
		}
		{
			final Set<String> middleUsedFront = new HashSet<>();
			front().forEach(p -> p.finish(this, middleUsedFront));
			final Set<String> missing = Sets.difference(middle().keySet(), middleUsedFront);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by front parts.",
						this,
						missing
				));
		}
	}

	public com.zarbosoft.pidgoon.internal.Node buildBackRule(final Syntax syntax) {
		final Sequence seq = new Sequence();
		seq.add(new BakedOperator((store) -> store.pushStack(0)));
		back().forEach(p -> seq.add(p.buildBackRule(syntax, this)));
		return new BakedOperator(seq, store -> {
			final Map<String, DataElement.Value> data = new HashMap<>();
			store = (Store) Helper.<Pair<String, DataElement.Value>>stackPopSingleList(store,
					pair -> data.put(pair.first, pair.second)
			);
			final Node node = new Node(this, data);
			return store.pushStack(node);
		});
	}

	public NodeTypeVisual createVisual(final Context context, final Map<String, DataElement.Value> data) {
		return new NodeTypeVisual(context, data);
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

	public DataRecord getDataRecord(final String middle) {
		return getData(DataRecord.class, middle);
	}

	public class NodeTypeVisual extends VisualNode {
		private final GroupVisualNode body;
		private boolean compact;
		private VisualNodeParent parent;
		public Map<String, VisualNodePart> frontToData = new HashMap<>();

		public NodeTypeVisual(final Context context, final Map<String, DataElement.Value> data) {
			super(HashTreePSet.<Tag>empty().plus(new TypeTag(id)).plus(new PartTag("node")));
			final PSet<Tag> tags = HashTreePSet.singleton(new TypeTag(id));
			compact = false;
			body = new GroupVisualNode(ImmutableSet.of());
			for (final Map.Entry<String, AlignmentDefinition> entry : alignments().entrySet()) {
				body.alignments.put(entry.getKey(), entry.getValue().create());
			}
			enumerate(stream(front())).forEach(pair -> {
				final VisualNodePart visual = pair.second.createVisual(context, data, tags);
				frontToData.put(pair.second.middle(), visual);
				body.add(context, visual);
			});
			body.setParent(new VisualNodeParent() {

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
				public VisualNode getTarget() {
					return parent.getTarget();
				}

				@Override
				public NodeType.NodeTypeVisual getNode() {
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
				public Context.Hoverable hover(
						final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector point
				) {
					if (parent == null)
						return null;
					return parent.hover(context, point);
				}
			});
		}

		@Override
		public void setParent(final VisualNodeParent parent) {
			this.parent = parent;
		}

		@Override
		public VisualNodeParent parent() {
			return body.parent();
		}

		@Override
		public boolean select(final Context context) {
			return body.select(context);
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
		public void destroyBricks(final Context context) {
			body.destroyBricks(context);
		}

		public NodeType getType() {
			return NodeType.this;
		}
	}

	private <D extends DataElement> D getData(final Class<? extends DataElement> type, final String id) {
		final DataElement found = middle().get(id);
		if (found == null) {
			throw new InvalidSyntax(String.format("No data field named [%s] in %s", id, this));
		} else {
			if (!type.isAssignableFrom(found.getClass()))
				throw new InvalidSyntax(String.format("Conflicting types for data field %s in %s: %s, %s",
						id,
						this,
						found.getClass(),
						type
				));
		}
		return (D) found;
	}

	public DataPrimitive getDataPrimitive(final String key) {
		return getData(DataPrimitive.class, key);
	}

	public DataNode getDataNode(final String key) {
		return getData(DataNode.class, key);
	}

	public DataArray getDataArray(final String key) {
		return getData(DataArray.class, key);
	}

}
