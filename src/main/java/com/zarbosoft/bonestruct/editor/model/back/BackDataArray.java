package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LArrayCloseEvent;
import com.zarbosoft.luxem.read.source.LArrayOpenEvent;
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

@Configuration(name = "data-array")
public class BackDataArray extends BackPart {
	@Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Operator(new Terminal(new LArrayOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new Operator(new Reference(nodeType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new Terminal(new LArrayCloseEvent()));
		return new Operator(sequence, (store) -> {
			final List<com.zarbosoft.bonestruct.editor.model.Node> temp = new ArrayList<>();
			store =
					(Store) com.zarbosoft.pidgoon.internal.Helper.<com.zarbosoft.bonestruct.editor.model.Node>stackPopSingleList(
							store,
							temp::add
					);
			Collections.reverse(temp);
			final DataArray.Value value = new DataArray.Value(nodeType.getDataArray(middle), temp);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	@Override
	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		nodeType.getDataArray(middle);
	}
}
