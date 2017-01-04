package com.zarbosoft.bonestruct.editor.model;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.*;
import com.zarbosoft.bonestruct.editor.visual.Brick;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.editor.visual.alignment.AlignmentDefinition;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNodeParent;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.GroupVisualNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Sequence;
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;

import java.util.*;

@Luxem.Configuration
public class NodeType {
	@Luxem.Configuration
	public String id;

	@Luxem.Configuration
	public String name;

	@Luxem.Configuration
	public List<FrontPart> front;

	@Luxem.Configuration
	public List<BackPart> back;

	@Luxem.Configuration
	public Map<String, DataElement> middle;

	@Luxem.Configuration
	public Map<String, AlignmentDefinition> alignments;

	@Luxem.Configuration(name = "space-priority", optional = true)
	public int spacePriority = 0;

	public com.zarbosoft.pidgoon.internal.Node buildLoadRule() {
		final Sequence seq = new Sequence();
		seq.add(new BakedOperator((store) -> store.pushStack(0)));
		back.forEach(p -> seq.add(p.buildLoadRule()));
		return new BakedOperator(seq, store -> {
			final Map<String, Object> data = new HashMap<>();
			store = (Store) Helper.<Pair<String, Object>>stackPopSingleList(store,
					pair -> data.put(pair.first, pair.second)
			);
			final Node node = new Node();
			node.data = data;
			node.type = this;
			return store.pushStack(node);
		});
	}

	public void finish(final Set<String> singleNodes, final Set<String> arrayNodes) {
		middle.forEach((k, v) -> v.id = k);
		{
			final Set<String> middleUsedBack = new HashSet<>();
			back.forEach(p -> p.finish(this, middleUsedBack));
			final Set<String> missing = Sets.difference(middle.keySet(), middleUsedBack);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by back parts.",
						this,
						missing
				));
		}
		{
			final Set<String> middleUsedFront = new HashSet<>();
			front.forEach(p -> p.finish(this, middleUsedFront));
			final Set<String> missing = Sets.difference(middle.keySet(), middleUsedFront);
			if (!missing.isEmpty())
				throw new InvalidSyntax(String.format("Data elements %s in %s are unused by front parts.",
						this,
						missing
				));
		}
	}

	private <D extends DataElement> D getData(final Class<? extends DataElement> type, final String id) {
		final DataElement found = middle.get(id);
		if (found == null) {
			throw new InvalidSyntax(String.format("No data field named [%s] in %s", id, this));
			/*
			found = Helper.uncheck(type::newInstance);
			found.id = id;
			middle.add(found);
			*/
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

	public DataRecord getDataRecord(final String key) {
		return getData(DataRecord.class, key);
	}

	public VisualNode createVisual(final Context context, final Map<String, Object> data) {
		return new NodeTypeVisual(context, data);
	}

	@Override
	public String toString() {
		return id;
	}

	private class NodeTypeVisual extends VisualNode {
		private final GroupVisualNode body;
		private boolean compact;
		private VisualNodeParent parent;

		public NodeTypeVisual(final Context context, final Map<String, Object> data) {
			super(HashTreePSet.<Tag>empty().plus(new TypeTag(id)).plus(new PartTag("node")));
			final PSet<Tag> tags = HashTreePSet.singleton(new TypeTag(id));
			compact = false;
			body = new GroupVisualNode(ImmutableSet.of());
			for (final Map.Entry<String, AlignmentDefinition> entry : alignments.entrySet()) {
				body.alignments.put(entry.getKey(), entry.getValue().create());
			}
			Helper.enumerate(front.stream()).forEach(pair -> {
				body.add(context, pair.second.createVisual(context, data, tags));
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
				public VisualNode getNode() {
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
				public Context.Hoverable hover(final Context context) {
					if (parent == null)
						return null;
					return parent.hover(context);
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

			/*
			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				return body.hover(context, point);
			}
			*/

		@Override
		public boolean select(final Context context) {
			return body.select(context);
		}

		@Override
		public Brick createFirstBrick(final Context context) {
			return body.createFirstBrick(context);
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
		public String debugTreeType() {
			return String.format("node type 0@%s", Integer.toHexString(hashCode()));
		}

		public String debugTree(final int indent) {
			final String indentString = String.join("", Collections.nCopies(indent, "  "));
			return String.format("%s%s\n%s", indentString, debugTreeType(), body.debugTree(indent + 1));
		}

		@Override
		public int spacePriority() {
			return spacePriority;
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
	}
}
