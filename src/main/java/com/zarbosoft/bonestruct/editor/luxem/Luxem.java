package com.zarbosoft.bonestruct.editor.luxem;

import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.AbortParse;
import com.zarbosoft.pidgoon.bytes.Grammar;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import javafx.scene.paint.Color;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Luxem extends com.zarbosoft.luxemj.Luxem {
	static private Luxem instance = null;

	static private Luxem get() {
		if (instance == null)
			instance = new Luxem();
		return instance;
	}

	static public Grammar grammarForType(final Class<?> target) {
		return get().implementationGrammarForType(target);
	}

	@Override
	public Node implementationNodeForType(final HashSet<Type> seen, final Grammar grammar, final TypeInfo target) {
		if (target.inner == Color.class) {
			return new BakedOperator(new Sequence()
					.add(new Terminal(new LArrayOpenEvent()))
					.add(new Repeat(new BakedOperator(new Terminal(new LPrimitiveEvent(null)), s -> {
						LPrimitiveEvent event = (LPrimitiveEvent) s.top();
						s = (Store) s.pushStack(Double.parseDouble(event.value));
						return s;
					})).min(3).max(3))
					.add(new Terminal(new LArrayCloseEvent())), s -> {
				final Double blue = s.stackTop();
				s = (Store) s.popStack();
				final Double green = s.stackTop();
				s = (Store) s.popStack();
				final Double red = s.stackTop();
				s = (Store) s.popStack();
				return s.pushStack(new Color(red, green, blue, 1));
			});
		} else if (target.inner == Duration.class) {
			return new BakedOperator(new Terminal(new LPrimitiveEvent(null)), s -> {
				Matcher match;
				match = Pattern.compile("(\\d+(.\\d+)?)\\s*[sS]").matcher(((LPrimitiveEvent) s.top()).value);
				if (match.matches()) {
					return s.pushStack(Duration.ofMillis((int) (Double.parseDouble(match.group()) * 1000)));
				}
				match = Pattern.compile("(\\d+(.\\d+)?)\\s*[mM]").matcher(((LPrimitiveEvent) s.top()).value);
				if (match.matches()) {
					return s.pushStack(Duration.ofMillis((int) (Double.parseDouble(match.group()) * 1000 * 60)));
				}
				throw new AbortParse("Duration must be a number suffixed with m (minutes) or s(seconds).");
			});
		}
		return super.implementationNodeForType(seen, grammar, target);
	}
}
