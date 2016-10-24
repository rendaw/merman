package com.zarbosoft.bonestruct.model;

import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.bonestruct.model.back.BackPart;
import com.zarbosoft.bonestruct.model.front.FrontPart;
import com.zarbosoft.bonestruct.model.middle.*;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.alignment.AlignmentDefinition;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.GroupVisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.VisualNodeParent;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Sequence;

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
		class Visual extends GroupVisualNode {
			@Override
			public Break breakMode() {
				//return Break.NEVER;
				return null;
			}

			@Override
			public String alignmentName() {
				throw new AssertionError("Not implemented.");
			}

			@Override
			public String alignmentNameCompact() {
				throw new AssertionError("Not implemented.");
			}

			@Override
			public String debugTreeType() {
				return String.format("node type@%s (%s)", Integer.toHexString(hashCode()), id);
			}
		}
		final Visual out = new Visual();
		for (final Map.Entry<String, AlignmentDefinition> entry : alignments.entrySet()) {
			out.alignments.put(entry.getKey(), entry.getValue().create());
		}
		for (final FrontPart part : front) {
			out.add(context, part.createVisual(context, data));
		}
		return new VisualNode() {
			@Override
			public void setParent(final VisualNodeParent parent) {
				out.setParent(parent);
			}

			@Override
			public VisualNodeParent parent() {
				return out.parent();
			}

			@Override
			public Context.Hoverable hover(final Context context, final Vector point) {
				return out.hover(context, point);
			}

			@Override
			public int startConverse(final Context context) {
				return out.startConverse(context);
			}

			@Override
			public int startTransverse(final Context context) {
				return out.startTransverse(context);
			}

			@Override
			public int startTransverseEdge(final Context context) {
				return out.startTransverseEdge(context);
			}

			@Override
			public int endConverse(final Context context) {
				return out.endConverse(context);
			}

			@Override
			public int endTransverse(final Context context) {
				return out.endTransverse(context);
			}

			@Override
			public int endTransverseEdge(final Context context) {
				return out.endTransverseEdge(context);
			}

			@Override
			public void place(final Context context, final Placement placement) {
				out.place(context, placement);
			}

			@Override
			public int edge(final Context context) {
				return out.edge(context);
			}

			@Override
			public Layer visual() {
				return out.visual();
			}

			@Override
			public void compact(final Context context) {
				out.compact(context);
			}

			@Override
			public String debugTreeType() {
				return String.format("node type 0@%s", Integer.toHexString(hashCode()));
			}

			public String debugTree(final int indent) {
				final String indentString = String.join("", Collections.nCopies(indent, "  "));
				return String.format("%s%s\n%s", indentString, debugTreeType(), out.debugTree(indent + 1));
			}
		};
	}

	@Override
	public String toString() {
		return id;
	}
}
