package com.zarbosoft.bonestruct.syntax;

import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNodeType;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.*;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.util.*;

import static com.zarbosoft.rendaw.common.Common.enumerate;

@Configuration
public abstract class NodeType {
	@Configuration
	public String id;

	@Configuration(name = "depth_score", optional = true,
			description = "Child nodes of this node are deeper by this amount.")
	public int depthScore = 0;

	public abstract List<FrontPart> front();

	public abstract Map<String, MiddleElement> middle();

	public abstract List<BackPart> back();

	public abstract Map<String, AlignmentDefinition> alignments();

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

	public VisualNodeType createVisual(final Context context, final Node node) {
		return new VisualNodeType(this, context, node);
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
