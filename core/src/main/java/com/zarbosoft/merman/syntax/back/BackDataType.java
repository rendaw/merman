package com.zarbosoft.merman.syntax.back;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.editor.backevents.ETypeEvent;
import com.zarbosoft.merman.syntax.AtomType;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.middle.MiddlePrimitive;
import com.zarbosoft.merman.syntax.middle.primitive.Pattern;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

@Configuration(name = "data_type")
public class BackDataType extends BackPart {
	@Configuration
	public String type;

	@Configuration
	public BackPart value;

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		final MiddlePrimitive middle = atomType.getDataPrimitive(this.type);
		final Pattern.Matcher matcher = middle.pattern.new Matcher();
		return new Sequence().add(new Operator(new Terminal() {
			@Override
			protected boolean matches(final Event event) {
				return event instanceof ETypeEvent && matcher.match(((ETypeEvent) event).value);
			}
		}, store -> {
			store = (Store) store.pushStack(new Pair<>(this.type,
					new ValuePrimitive(middle, ((ETypeEvent) store.top()).value)
			));
			return Helper.stackSingleElement(store);
		})).add(value.buildBackRule(syntax, atomType));
	}

	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		middleUsed.add(type);
		atomType.getDataPrimitive(type);
		value.finish(syntax, atomType, middleUsed);
		value.parent = new PartParent() {
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
