package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;

import java.util.Set;

@Luxem.Configuration(name = "data-node")
public class BackDataNode implements BackPart {
	@Luxem.Configuration
	public String middle;
	private DataNode dataType;

	public Node buildLoadRule() {
		return new BakedOperator(new Reference(dataType.tag), (store) -> {
			final DataNode.Value value = store.stackTop();
			store = (Store) store.popStack();
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataNode(middle);
	}
}
