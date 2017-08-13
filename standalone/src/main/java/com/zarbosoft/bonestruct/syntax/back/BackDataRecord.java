package com.zarbosoft.bonestruct.syntax.back;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.middle.MiddleRecord;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LObjectCloseEvent;
import com.zarbosoft.luxem.read.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;
import static com.zarbosoft.rendaw.common.Common.last;

@Configuration(name = "data_record")
public class BackDataRecord extends BackPart {
	@Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Operator(new MatchingEventTerminal(new LObjectOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new Operator(new Reference(atomType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new MatchingEventTerminal(new LObjectCloseEvent()));
		return new Operator(sequence, (store) -> {
			final List<Atom> temp = new ArrayList<>();
			store = (Store) com.zarbosoft.pidgoon.internal.Helper.<Atom>stackPopSingleList(store, temp::add);
			Collections.reverse(temp);
			final ValueArray value = new ValueArray(atomType.getDataArray(middle), temp);
			store = (Store) store.pushStack(new Pair<>(middle, value));
			return Helper.stackSingleElement(store);
		});
	}

	private static void finishThrow(final AtomType type) {
		throw new InvalidSyntax(String.format("As the element type of a back record, [%s] must have exactly a back key, followed by a back type " +
						"then value or just a back value.",
				type.id
		));
	}

	@Override
	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final MiddleRecord dataType = atomType.getDataRecord(middle);
		for (final FreeAtomType element : iterable(syntax.getLeafTypes(dataType.type))) {
			if (element.back.size() < 1)
				finishThrow(element);
			if (!(element.back.get(0) instanceof BackDataKey))
				finishThrow(element);
			if (element.back.size() >= 2 && element.back.size() <= 3) {
				final BackPart lastBack = last(element.back);
				if (ImmutableList
						.of(BackDataKey.class, BackType.class)
						.stream()
						.anyMatch(klass -> klass.equals(lastBack.getClass())))
					finishThrow(element);
				if (element.back.size() == 3 && !(element.back.get(1) instanceof BackType))
					finishThrow(element);
			} else
				finishThrow(element);
		}
	}
}
