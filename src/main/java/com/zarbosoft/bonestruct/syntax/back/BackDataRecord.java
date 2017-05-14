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
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
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

@Configuration(name = "data_record")
public class BackDataRecord extends BackPart {
	@Configuration
	public String middle;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		final Sequence sequence;
		sequence = new Sequence();
		sequence.add(new Operator(new Terminal(new LObjectOpenEvent()), (store) -> store.pushStack(0)));
		sequence.add(new Repeat(new Operator(
				new Reference(atomType.getDataArray(middle).type),
				(store) -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
		)));
		sequence.add(new Terminal(new LObjectCloseEvent()));
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
		final MiddleRecord dataType = atomType.getDataRecord(middle);
		for (final FreeAtomType element : iterable(syntax.getLeafTypes(dataType.type))) {
			if (element.back.size() != 2 ||
					!(element.back.get(0) instanceof BackDataKey) ||
					ImmutableList
							.of(BackDataAtom.class, BackDataPrimitive.class, BackDataRecord.class, BackDataArray.class)
							.stream()
							.noneMatch(klass -> klass.equals(element.back.get(1).getClass())))
				throw new InvalidSyntax(
						"The element type of a back record must have exactly two back parts itself, the first which is a back key.");
		}
	}
}
