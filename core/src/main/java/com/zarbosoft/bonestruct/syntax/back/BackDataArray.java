package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.editor.backevents.EArrayCloseEvent;
import com.zarbosoft.bonestruct.editor.backevents.EArrayOpenEvent;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Configuration(name = "data_array")
public class BackDataArray extends BackPart {
	@Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Operator(new MatchingEventTerminal(new EArrayOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new Operator(
				syntax.backRuleRef(atomType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new MatchingEventTerminal(new EArrayCloseEvent()));
		return new Operator(sequence, (store) -> {
			final List<Atom> temp = new ArrayList<>();
			store = (Store) com.zarbosoft.pidgoon.internal.Helper.<Atom>stackPopSingleList(store, temp::add);
			Collections.reverse(temp);
			final ValueArray value = new ValueArray(atomType.getDataArray(middle), temp);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	@Override
	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		atomType.getDataArray(middle);
	}
}
