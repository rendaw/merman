package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.document.values.ValueRecordKey;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LKeyEvent;
import com.zarbosoft.luxem.read.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

@Configuration(name = "data_key")
public class BackDataKey extends BackPart {
	@Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new Operator(new Terminal(new LKeyEvent(null)), store -> {
			store = (Store) store.pushStack(new Pair<>(
					middle,
					new ValueRecordKey(nodeType.getDataRecordKey(middle), ((LPrimitiveEvent) store.top()).value)
			));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		nodeType.getDataPrimitive(middle);
	}
}
