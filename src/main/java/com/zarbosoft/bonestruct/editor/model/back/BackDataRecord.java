package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataRecord;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.source.LKeyEvent;
import com.zarbosoft.luxemj.source.LObjectCloseEvent;
import com.zarbosoft.luxemj.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.Set;

@Luxem.Configuration(name = "data-record")
public class BackDataRecord implements BackPart {
	@Luxem.Configuration
	public String middle;
	private DataRecord dataType;

	@Override
	public Node buildLoadRule() {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new BakedOperator(new Terminal(new LObjectOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new BakedOperator(new Sequence().add(new BakedOperator(
				new Terminal(new LKeyEvent(null)),
				store -> store.pushStack(new SimpleStringProperty(((LKeyEvent) store.top()).value))
		)).add(new Reference(dataType.tag)), (store) -> Helper.stackDoubleElement(store))));
		sequence.add(new Terminal(new LObjectCloseEvent()));
		return new BakedOperator(sequence, (store) -> {
			final ObservableList<Object> value = FXCollections.observableArrayList();
			store = (Store) Helper.stackPopSingleList(store, value::add);
			Collections.reverse(value);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	@Override
	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		dataType = nodeType.getDataRecord(middle);
	}
}
