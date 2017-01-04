package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.beans.property.SimpleStringProperty;

import java.util.Set;

@Luxem.Configuration(name = "data-primitive")
public class BackDataPrimitive implements BackPart {
	@Luxem.Configuration
	public String middle;

	@Override
	public Node buildLoadRule() {
		return new BakedOperator(new Terminal(new LPrimitiveEvent(null)), store -> {
			store = (Store) store.pushStack(new Pair<>(middle,
					new SimpleStringProperty(((LPrimitiveEvent) store.top()).value)
			));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		nodeType.getDataPrimitive(middle);
	}
}
