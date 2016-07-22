package com.zarbosoft.bonestruct.model.back;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataArray;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
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
public class BackDataArray implements BackPart {
	@Luxem.Configuration
	public String key;
	private DataArray dataType;

	@Override
	public Node buildLoadRule() {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new BakedOperator(new Terminal(new LArrayOpenEvent()), (store) -> {
			return store.pushStack(0);
		}));
		sequence.add(new Repeat(new BakedOperator(new Reference(dataType.tag), (store) -> {
			return com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store);
		})));
		sequence.add(new Terminal(new LArrayCloseEvent()));
		return new BakedOperator(sequence, (store) -> {
			final List<Object> value = new ArrayList<>();
			store = (Store) com.zarbosoft.pidgoon.internal.Helper.stackPopSingleList(store, value::add);
			Collections.reverse(value);
			return store.pushStack(new Pair<>(key, value));
		});
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		dataType = nodeType.getDataArray(key);
	}
}
