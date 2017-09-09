package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

@Configuration(name = "data_type")
public class BackDataType extends BackPart {
	@Configuration
	public String middle;

	@Configuration
	public BackPart child;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new Sequence().add(new Operator(new MatchingEventTerminal(new ETypeEvent(null)), store -> {
			store = (Store) store.pushStack(new Pair<>(middle,
					new ValuePrimitive(atomType.getDataPrimitive(middle), ((ETypeEvent) store.top()).value)
			));
			return Helper.stackSingleElement(store);
		})).add(child.buildBackRule(syntax, atomType));
	}

	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		atomType.getDataPrimitive(middle);
		child.finish(syntax, atomType, middleUsed);
		child.parent = new PartParent() {
			@Override
			public BackPart part() {
				return BackDataType.this;
			}

			@Override
			public String pathSection() {
				return null;
			}
		};
	}
}
