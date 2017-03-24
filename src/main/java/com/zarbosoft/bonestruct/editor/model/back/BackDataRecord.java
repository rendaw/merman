package com.zarbosoft.bonestruct.editor.model.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataRecord;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LObjectCloseEvent;
import com.zarbosoft.luxem.read.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration(name = "data-record")
public class BackDataRecord extends BackPart {
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new BakedOperator(new Terminal(new LObjectOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new BakedOperator(new Reference(nodeType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new Terminal(new LObjectCloseEvent()));
		return new BakedOperator(sequence, (store) -> {
			final List<com.zarbosoft.bonestruct.editor.model.Node> temp = new ArrayList<>();
			store =
					(Store) com.zarbosoft.pidgoon.internal.Helper.<com.zarbosoft.bonestruct.editor.model.Node>stackPopSingleList(
							store,
							temp::add
					);
			Collections.reverse(temp);
			final DataRecord.Value value = new DataRecord.Value(nodeType.getDataArray(middle), temp);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	@Override
	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final DataRecord dataType = nodeType.getDataRecord(middle);
		for (final FreeNodeType element : syntax.getLeafTypes(dataType.type)) {
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
