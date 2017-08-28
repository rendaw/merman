package com.zarbosoft.bonestruct.editor.serialization;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.editor.backevents.*;
import com.zarbosoft.bonestruct.editor.serialization.json.JSONParse;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.luxem.read.Parse;
import com.zarbosoft.luxem.read.RawReader;
import com.zarbosoft.pidgoon.events.*;
import com.zarbosoft.pidgoon.internal.NamedOperator;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Repeat;
import com.zarbosoft.pidgoon.nodes.Sequence;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Load {
	public static Document load(final Syntax syntax, final Path path) throws FileNotFoundException, IOException {
		try (
				InputStream data = Files.newInputStream(path)
		) {
			return load(syntax, data);
		}
	}

	public static Document load(final Syntax syntax, final String string) {
		return load(syntax, new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	private static RawReader.EventFactory luxemEventFactory() {
		return new RawReader.EventFactory() {
			@Override
			public Event objectOpen() {
				return new EObjectOpenEvent();
			}

			@Override
			public Event objectClose() {
				return new EObjectCloseEvent();
			}

			@Override
			public Event arrayOpen() {
				return new EArrayOpenEvent();
			}

			@Override
			public Event arrayClose() {
				return new EArrayCloseEvent();
			}

			@Override
			public Event key(final String s) {
				return new EKeyEvent(s);
			}

			@Override
			public Event type(final String s) {
				return new ETypeEvent(s);
			}

			@Override
			public Event primitive(final String s) {
				return new EPrimitiveEvent(s);
			}
		};
	}

	public static Document load(final Syntax syntax, final InputStream data) {
		switch (syntax.backType) {
			case LUXEM:
				return new Document(syntax,
						new Parse<Atom>()
								.eventFactory(luxemEventFactory())
								.grammar(syntax.getGrammar())
								.eventUncertainty(1000)
								.parse(data)
				);
			case JSON:
				return new Document(syntax,
						new JSONParse<Atom>().grammar(syntax.getGrammar()).eventUncertainty(1000).parse(data)
				);
			default:
				throw new DeadCode();
		}
	}

	public static List<Atom> loadMultiple(final Syntax syntax, final String type, final InputStream data) {
		switch (syntax.backType) {
			case LUXEM: {
				final Grammar grammar = new Grammar(syntax.getGrammar());
				grammar.add(new NamedOperator(data, new Operator(new Repeat(new Operator(new Reference(type),
						store -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
				)).max(7), store -> {
					final List<Atom> temp = new ArrayList<>();
					store = (Store) com.zarbosoft.pidgoon.internal.Helper.<Atom>stackPopSingleList(store, temp::add);
					Collections.reverse(temp);
					return store.pushStack(temp);
				})));
				return new Parse<List<Atom>>()
						.grammar(grammar)
						.eventFactory(luxemEventFactory())
						.stack(() -> 0)
						.root(data)
						.eventUncertainty(1000)
						.parse(data);
			}
			case JSON: {
				final Grammar grammar = new Grammar(syntax.getGrammar());
				grammar.add(new NamedOperator(data,
						new Sequence()
								.add(new MatchingEventTerminal(new EArrayOpenEvent()))
								.add(new Operator(new Repeat(new Operator(new Reference(type),
										store -> com.zarbosoft.pidgoon.internal.Helper.stackSingleElement(store)
								)).max(7), store -> {
									final List<Atom> temp = new ArrayList<>();
									store =
											(Store) com.zarbosoft.pidgoon.internal.Helper.<Atom>stackPopSingleList(store,
													temp::add
											);
									Collections.reverse(temp);
									return store.pushStack(temp);
								}))
								.add(new MatchingEventTerminal(new EArrayCloseEvent()))
				));
				return new JSONParse<List<Atom>>()
						.grammar(grammar)
						.stack(() -> 0)
						.root(data)
						.eventUncertainty(1000)
						.parse(data);
			}
			default:
				throw new DeadCode();
		}
	}
}
