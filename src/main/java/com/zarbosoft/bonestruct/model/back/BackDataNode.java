package com.zarbosoft.bonestruct.model.back;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.bonestruct.model.middle.DataNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;

import java.util.Set;

@Luxem.Configuration(name = "data-node")
public class BackDataNode implements BackPart {
	@Luxem.Configuration
	public String key;
	private DataNode dataType;

	public Node buildLoadRule() {
		return new BakedOperator(new Reference(dataType.tag), (store) -> {
			final Object value = store.stackTop();
			store = (Store) store.popStack();
			return store.pushStack(new Pair<>(key, value));
		});
	}

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		dataType = nodeType.getDataNode(key);
	}
}
