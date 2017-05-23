package com.zarbosoft.bonestruct.document;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zarbosoft.bonestruct.document.values.*;
import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.MiddlePart;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.reverse;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Document {

	final public Syntax syntax;
	public ValueArray rootArray;
	public Atom root;

	public Document(final Syntax syntax, final ValueArray rootArray) {
		this.syntax = syntax;
		this.rootArray = rootArray;
		root = new Atom(new AtomType() {
			@Override
			public List<FrontPart> front() {
				return ImmutableList.of(syntax.rootFront);
			}

			@Override
			public Map<String, MiddlePart> middle() {
				return ImmutableMap.of("value", syntax.root);
			}

			@Override
			public List<BackPart> back() {
				return null;
			}

			@Override
			public Map<String, AlignmentDefinition> alignments() {
				return syntax.rootAlignments;
			}

			@Override
			public int precedence() {
				return Integer.MIN_VALUE;
			}

			@Override
			public boolean frontAssociative() {
				return false;
			}

			@Override
			public String name() {
				return "root array";
			}
		}, ImmutableMap.of("value", rootArray));
	}

	public void write(final Path out) {
		uncheck(() -> {
			try (OutputStream stream = Files.newOutputStream(out)) {
				final RawWriter writer = new RawWriter(stream, (byte) ' ', 4);
				rootArray.data.forEach(node -> write(node, writer));
				stream.write('\n');
				stream.flush();
			}
		});
	}

	private static class WriteKey {
		public final String key;

		private WriteKey(final String key) {
			this.key = key;
		}
	}

	public static void write(final Atom atom, final RawWriter writer) {
		uncheck(() -> {
			final Deque<Pair<Object, Map<String, Value>>> stack = new ArrayDeque<>();
			stack.addLast(new Pair<>(atom, null));
			while (!stack.isEmpty()) {
				final Pair<Object, Map<String, Value>> pair = stack.pollLast();
				final Map<String, Value> data = pair.second;
				final Object next = pair.first;
				if (next instanceof Atom) {
					for (final BackPart part : reverse(((Atom) next).type.back())) {
						stack.addLast(new Pair<>(part, ((Atom) next).data));
					}
				} else if (next instanceof BackType) {
					writer.type(((BackType) next).value);
				} else if (next instanceof BackPrimitive) {
					writer.primitive(((BackPrimitive) next).value);
				} else if (next instanceof BackArray) {
					writer.arrayBegin();
					for (final BackPart part : reverse(((BackArray) next).elements)) {
						stack.addLast(new Pair<>(part, data));
					}
					writer.arrayEnd();
				} else if (next instanceof BackRecord) {
					writer.recordBegin();
					for (final Map.Entry<String, BackPart> entry : ((BackRecord) next).pairs.entrySet()) {
						stack.addLast(new Pair<>(entry.getValue(), data));
						stack.addLast(new Pair<>(new WriteKey(entry.getKey()), data));
					}
					writer.recordEnd();
				} else if (next instanceof WriteKey) {
					writer.key(((WriteKey) next).key);
				} else if (next instanceof BackDataPrimitive) {
					writer.primitive(((ValuePrimitive) data.get(((BackDataPrimitive) next).middle)).get());
				} else if (next instanceof BackDataAtom) {
					stack.addLast(new Pair<>(((ValueAtom) data.get(((BackDataAtom) next).middle)).get(), null));
				} else if (next instanceof BackDataArray) {
					for (final Atom atom2 : reverse(((ValueArray) data.get(((BackDataArray) next).middle)).data)) {
						stack.addLast(new Pair<>(atom2, null));
					}
				} else if (next instanceof BackDataKey) {
					writer.key(((ValueRecordKey) data.get(((BackDataKey) next).middle)).get());
				} else if (next instanceof BackDataRecord) {
					for (final Atom atom2 : ((ValueArray) data.get(((BackDataRecord) next).middle)).data) {
						stack.addLast(new Pair<>(atom2, null));
					}
				} else
					throw new DeadCode();
			}
		});
	}
}
