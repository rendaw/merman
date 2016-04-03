package com.zarbosoft.bonestruct.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.reflections.Reflections;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.zarbosoft.bonestruct.Config;
import com.zarbosoft.bonestruct.Helper;
import com.zarbosoft.luxemj.source.LArrayCloseEvent;
import com.zarbosoft.luxemj.source.LArrayOpenEvent;
import com.zarbosoft.luxemj.source.LKeyEvent;
import com.zarbosoft.luxemj.source.LObjectCloseEvent;
import com.zarbosoft.luxemj.source.LObjectOpenEvent;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.luxemj.source.LTypeEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Event;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Parse;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.pidgoon.nodes.Set;
import com.zarbosoft.pidgoon.nodes.Union;
import com.zarbosoft.pidgoon.nodes.Wildcard;

public class Syntax {
	private static Reflections reflections = new Reflections("com.zarbosoft");
	
	@Config
	public String name;
	
	@Config
	public List<Event> template;
	
	@Config
	public List<NodeType> types;
	
	Grammar grammar;
	
	public static Grammar syntaxGrammar() {
		Grammar grammar = new Grammar();
		grammar.add("syntax", buildLoadRule(Syntax.class));
		return grammar;
	}
	
	private Grammar getGrammar() {
		if (grammar == null) {
			Union union = new Union();
			types.forEach(t -> {
				union.add(new BakedOperator(
					t.buildLoadRule(),
					s -> {
						Object temp = s.popStack();
						Integer count = (Integer) s.popStack();
						s.pushStack(temp);
						s.pushStack(count + 1);
					}
				));
			});
			grammar = new Grammar();
			grammar.add(
				"root",
				new BakedOperator(
					union,
					s -> {
						List<Node> out = new ArrayList<>();
						Helper.<Node>popSingleList(
							s,
							v -> {
								out.add(v);
							}
						);
						s.pushStack(out);
					}
				)
			);
		}
		return grammar;
	}
	
	private static com.zarbosoft.pidgoon.internal.Node buildLoadRule(Type klass) {
		if (klass == String.class) {
			return new BakedOperator(
				new Terminal(new LPrimitiveEvent(null, null)),
				s -> {
					LPrimitiveEvent event = (LPrimitiveEvent) s.top();
					s.pushStack(event.value);
				}
			);
		} else if (klass == int.class) {
			return new BakedOperator(
				new Terminal(new LPrimitiveEvent(null, null)),
				s -> {
					LPrimitiveEvent event = (LPrimitiveEvent) s.top();
					s.pushStack(Integer.valueOf(event.value));
				}
			);
		} else if ((klass == double.class) || (klass == Double.class)) {
			return new BakedOperator(
				new Terminal(new LPrimitiveEvent(null, null)),
				s -> {
					LPrimitiveEvent event = (LPrimitiveEvent) s.top();
					s.pushStack(Double.valueOf(event.value));
				}
			);
		} else if (klass == boolean.class) {
			return new BakedOperator(
				new Terminal(new LPrimitiveEvent(null, null)),
				s -> {
					LPrimitiveEvent event = (LPrimitiveEvent) s.top();
					if (event.value.equals("true"))
						s.pushStack(true);
					else if (event.value.equals("false"))
						s.pushStack(false);
					else throw new RuntimeException(String.format("Invalid value [%s] for field type [%s]", event.value, klass));
				}
			);
		} else if (klass == Event.class) {
			return new BakedOperator(
				new Wildcard(),
				s -> s.pushStack(s.top())
			);
		} else if (List.class.isAssignableFrom((Class<?>)klass)) {
			Class<?> innerType = (Class<?>)((ParameterizedType)klass).getActualTypeArguments()[0];
			return new BakedOperator(
				new Sequence()
					.add(new Terminal(new LArrayOpenEvent()))
					.add(new Repeat(
						new BakedOperator(
							buildLoadRule(innerType),
							s -> {
								Object temp = s.popStack();
								Integer count = (Integer) s.popStack();
								s.pushStack(temp);
								s.pushStack(count + 1);
							}
						)
					))
					.add(new Terminal(new LArrayCloseEvent())),
				s -> {
					@SuppressWarnings("rawtypes")
					List out = (List) Helper.uncheck(() -> ((Class<?>)klass).newInstance());
					Helper.popSingleList(
						s,
						v -> {
							out.add(v);
						}
					);
					s.pushStack(out);
				}
			);
		} else {
			if (((Class<?>)klass).isInterface()) {
				Union out = new Union();
				Sets.difference(reflections.getSubTypesOf((Class<?>) klass), ImmutableSet.of(klass)).stream()
					.map(s -> (Class<?>)s)
					.filter(s -> !Modifier.isAbstract(s.getModifiers()))
					.forEach(s -> {
						out.add(
							new Sequence()
								.add(new Terminal(new LTypeEvent(s.getName().toLowerCase())))
								.add(buildLoadRule(s))
						);
					});
				return out;
			} else {
				Field[] fields = ((Class<?>)klass).getFields();
				if (Helper.stream(fields).anyMatch(f -> f.getAnnotation(Config.class) != null)) {
					Sequence seq = new Sequence();
					seq.add(new Terminal(new LObjectOpenEvent()));
					Set set = new Set();
					Helper.stream(fields)
						.filter(f -> f.getAnnotation(Config.class) != null)
						.forEach(f -> {
							set.add(new BakedOperator(
								new Sequence()
									.add(new Terminal(new LKeyEvent(f.getName())))
									.add(buildLoadRule(f.getType())),
								s -> {
									s.pushStack(f);
								}
							));
						});
					seq.add(new Terminal(new LObjectCloseEvent()));
					return new BakedOperator(
						seq,
						s -> {
							Object out = Helper.uncheck(() -> ((Class<?>)klass).newInstance());
							Helper.<Field, Object>popDoubleList(
								s, 
								fields.length,
								(k, v) -> {
									Helper.uncheck(() -> k.set(out, v));
								});
							s.pushStack(out);
						}
					);
				}
			}
			throw new AssertionError(String.format("Unconfigurable field of type [%s]", klass));
		}
	}
	
	public Document create() {
		EventStream<List<Node>> stream = new Parse<List<Node>>()
			.grammar(getGrammar())
			.node("root")
			.parse();
		template.forEach(e -> stream.push(e));
		return new Document(this, stream.finish());
	}

	public Document load(File file) throws FileNotFoundException, IOException {
		try (
			FileInputStream fileStream = new FileInputStream(file);
		) {
			return new Document(
				this,
				new com.zarbosoft.luxemj.Parse<List<Node>>()
					.grammar(getGrammar())
					.node("root")
					.parse(fileStream)
			);
		}
	}
}
