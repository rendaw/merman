package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Luxem.Configuration(name = "data-array")
public class BackDataArray extends BackPart {
	@Luxem.Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new BakedOperator(new Terminal(new LArrayOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new BakedOperator(new Reference(nodeType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new Terminal(new LArrayCloseEvent()));
		return new BakedOperator(sequence, (store) -> {
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
