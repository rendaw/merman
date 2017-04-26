package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.*;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import static com.google.common.collect.Lists.reverse;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Document {

	final public Syntax syntax;
	final public ValueArray top;

	public Document(final Syntax syntax, final ValueArray top) {
		this.syntax = syntax;
		this.top = top;
	}

	public void write(final Path out) {
		uncheck(() -> {
			try (OutputStream stream = Files.newOutputStream(out)) {
				final RawWriter writer = new RawWriter(stream, (byte) ' ', 4);
				top.data.forEach(node -> write(node, writer));
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

	public static void write(final Node node, final RawWriter writer) {
		uncheck(() -> {
			final Deque<Pair<Object, Map<String, Value>>> stack = new ArrayDeque<>();
			stack.addLast(new Pair<>(node, null));
			while (!stack.isEmpty()) {
				final Pair<Object, Map<String, Value>> pair = stack.pollLast();
				final Map<String, Value> data = pair.second;
				final Object next = pair.first;
				if (next instanceof Node) {
					for (final BackPart part : reverse(((Node) next).type.back())) {
						stack.addLast(new Pair<>(part, ((Node) next).data));
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
				} else if (next instanceof BackDataNode) {
					stack.addLast(new Pair<>(((ValueNode) data.get(((BackDataNode) next).middle)).get(), null));
				} else if (next instanceof BackDataArray) {
					for (final Node node2 : reverse(((ValueArray) data.get(((BackDataArray) next).middle)).data)) {
						stack.addLast(new Pair<>(node2, null));
					}
				} else if (next instanceof BackDataKey) {
					writer.key(((ValueRecordKey) data.get(((BackDataKey) next).middle)).get());
				} else if (next instanceof BackDataRecord) {
					for (final Node node2 : ((ValueArray) data.get(((BackDataRecord) next).middle)).data) {
						stack.addLast(new Pair<>(node2, null));
					}
				} else
					throw new DeadCode();
			}
		});
	}
}
