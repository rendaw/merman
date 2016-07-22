package com.zarbosoft.bonestruct.model.back;

import com.zarbosoft.bonestruct.model.NodeType;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;

import java.util.Set;

@Luxem.Configuration(name = "data-primitive")
public class BackDataPrimitive implements BackPart {
	@Luxem.Configuration
	public String key;

	@Override
	public Node buildLoadRule() {
		return new BakedOperator(
				new Terminal(new LPrimitiveEvent(null)),
				store -> store.pushStack(new Pair<>(key, ((LPrimitiveEvent) store.top()).value))
		);
	}

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(key);
		nodeType.getDataPrimitive(key);
	}
}
