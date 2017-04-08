package com.zarbosoft.bonestruct.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LObjectCloseEvent;
import com.zarbosoft.luxem.read.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "data-record")
public class BackDataRecord extends BackPart {
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Operator(new Terminal(new LObjectOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new Operator(new Reference(nodeType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new Terminal(new LObjectCloseEvent()));
		return new Operator(sequence, (store) -> {
			final List<com.zarbosoft.bonestruct.document.Node> temp = new ArrayList<>();
			store =
					(Store) com.zarbosoft.pidgoon.internal.Helper.<com.zarbosoft.bonestruct.document.Node>stackPopSingleList(
							store,
							temp::add
					);
			Collections.reverse(temp);
			final ValueArray value = new ValueArray(nodeType.getDataArray(middle), temp);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	@Override
	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final MiddleRecord dataType = nodeType.getDataRecord(middle);
		for (final FreeNodeType element : iterable(syntax.getLeafTypes(dataType.type))) {
			if (element.back.size() != 2 ||
					!(element.back.get(0) instanceof BackDataKey) ||
					ImmutableList
							.of(BackDataNode.class, BackDataPrimitive.class, BackDataRecord.class, BackDataArray.class)
							.stream()
							.noneMatch(klass -> klass.equals(element.back.get(1).getClass())))
				throw new InvalidSyntax(
						"The element type of a back record must have exactly two back parts itself, the first which is a back key.");
		}
	}
}
