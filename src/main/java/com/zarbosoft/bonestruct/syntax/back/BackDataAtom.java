package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.middle.MiddleAtom;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "data_node")
public class BackDataAtom extends BackPart {
	@Configuration
	public String middle;

	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new Operator(new Reference(atomType.getDataNode(middle).type), (store) -> {
			final Atom value = store.stackTop();
			store = (Store) store.popStack();
			store = (Store) store.pushStack(new Pair<>(middle, new ValueAtom(atomType.getDataNode(middle), value)));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final MiddleAtom data = atomType.getDataNode(middle);
		for (final FreeAtomType child : iterable(syntax.getLeafTypes(data.type))) {
			if (child.back.size() > 1)
				throw new InvalidSyntax(String.format(
						"Type [%s] is a child of [%s] at middle [%s], but deserializes as an array segment.",
						child.id,
						atomType.id,
						middle
				));
		}
	}
}
