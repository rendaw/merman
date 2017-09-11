package com.zarbosoft.merman.editor.serialization;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.google.common.collect.ImmutableList;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.document.values.ValueAtom;
import com.zarbosoft.merman.document.values.ValuePrimitive;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.back.*;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Write {
	public static void write(final Document document, final Path out) {
		uncheck(() -> {
			try (OutputStream stream = Files.newOutputStream(out)) {
				write(document.root, document.syntax, stream);
			}
		});
	}

	private static EventConsumer luxemEventConsumer(final RawWriter writer) {
		return new EventConsumer() {
			@Override
			public void primitive(final String value) throws IOException {
				writer.primitive(value);
			}

			@Override
			public void type(final String value) throws IOException {
				writer.type(value);
			}

			@Override
			public void arrayBegin() throws IOException {
				writer.arrayBegin();
			}

			@Override
			public void arrayEnd() throws IOException {
				writer.arrayEnd();
			}

			@Override
			public void recordBegin() throws IOException {
				writer.recordBegin();
			}

			@Override
			public void recordEnd() throws IOException {
				writer.recordEnd();
			}

			@Override
			public void key(final String s) throws IOException {
				writer.key(s);
			}

			@Override
			public void jsonInt(final String value) {
				throw new AssertionError();
			}

			@Override
			public void jsonFloat(final String value) {
				throw new AssertionError();
			}

			@Override
			public void jsonTrue() {
				throw new AssertionError();
			}

			@Override
			public void jsonFalse() {
				throw new AssertionError();
			}

			@Override
			public void jsonNull() {
				throw new AssertionError();
			}
		};
	}

	private static EventConsumer jsonEventConsumer(final JsonGenerator generator) {
		return new EventConsumer() {
			@Override
			public void primitive(final String value) throws IOException {
				generator.writeString(value);
			}

			@Override
			public void type(final String value) throws IOException {
				throw new AssertionError();
			}

			@Override
			public void arrayBegin() throws IOException {
				generator.writeStartArray();
			}

			@Override
			public void arrayEnd() throws IOException {
				generator.writeEndArray();
			}

			@Override
			public void recordBegin() throws IOException {
				generator.writeStartObject();
			}

			@Override
			public void recordEnd() throws IOException {
				generator.writeEndObject();
			}

			@Override
			public void key(final String s) throws IOException {
				generator.writeFieldName(s);
			}

			@Override
			public void jsonInt(final String value) throws IOException {
				generator.writeRaw(value);
			}

			@Override
			public void jsonFloat(final String value) throws IOException {
				generator.writeRaw(value);
			}

			@Override
			public void jsonTrue() throws IOException {
				generator.writeBoolean(true);
			}

			@Override
			public void jsonFalse() throws IOException {
				generator.writeBoolean(false);
			}

			@Override
			public void jsonNull() throws IOException {
				generator.writeNull();
			}
		};
	}

	public static void write(final Atom atom, final Syntax syntax, final OutputStream stream) {
		uncheck(() -> {
			final EventConsumer writer;
			switch (syntax.backType) {
				case LUXEM:
					writer = luxemEventConsumer(syntax.prettySave ?
							new RawWriter(stream, (byte) ' ', 4) :
							new RawWriter(stream));
					break;
				case JSON: {
					final JsonGenerator generator = new JsonFactory().createGenerator(stream);
					if (syntax.prettySave)
						generator.setPrettyPrinter(new DefaultPrettyPrinter());
					writer = jsonEventConsumer(generator);
					break;
				}
				default:
					throw new DeadCode();
			}
			write(atom, writer);
			if (syntax.backType == Syntax.BackType.LUXEM && syntax.prettySave)
				stream.write('\n');
			stream.flush();
		});
	}

	public static void write(final List<Atom> atoms, final Syntax syntax, final OutputStream stream) {
		uncheck(() -> {
			JsonGenerator jsonGenerator = null;
			final EventConsumer writer;
			switch (syntax.backType) {
				case LUXEM:
					writer = luxemEventConsumer(syntax.prettySave ?
							new RawWriter(stream, (byte) ' ', 4) :
							new RawWriter(stream));
					break;
				case JSON: {
					jsonGenerator = new JsonFactory().createGenerator(stream);
					if (syntax.prettySave)
						jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
					jsonGenerator.writeStartArray();
					writer = jsonEventConsumer(jsonGenerator);
					break;
				}
				default:
					throw new DeadCode();
			}
			for (final Atom atom : atoms)
				write(atom, writer);
			switch (syntax.backType) {
				case LUXEM:
					if (syntax.prettySave)
						stream.write('\n');
					break;
				case JSON:
					jsonGenerator.writeEndArray();
					break;
				default:
					throw new DeadCode();
			}
			stream.flush();
		});
	}

	private static class WriteArrayEnd {
	}

	private static class WriteRecordEnd {
	}

	private static class WriteKey {
		public final String key;

		private WriteKey(final String key) {
			this.key = key;
		}
	}

	private static abstract class WriteState {
		public abstract void run(Deque<WriteState> stack, EventConsumer writer) throws IOException;
	}

	private interface EventConsumer {

		void primitive(String value) throws IOException;

		void type(String value) throws IOException;

		void arrayBegin() throws IOException;

		void arrayEnd() throws IOException;

		void recordBegin() throws IOException;

		void recordEnd() throws IOException;

		void key(String s) throws IOException;

		void jsonInt(String value) throws IOException;

		void jsonFloat(String value) throws IOException;

		void jsonTrue() throws IOException;

		void jsonFalse() throws IOException;

		void jsonNull() throws IOException;

	}

	private static void writePart(
			final Deque<WriteState> stack, final EventConsumer writer, final Atom base, final BackPart part
	) throws IOException {
		if (part instanceof BackPrimitive) {
			writer.primitive(((BackPrimitive) part).value);
		} else if (part instanceof BackJSONInt) {
			writer.jsonInt(((BackJSONInt) part).value);
		} else if (part instanceof BackJSONFloat) {
			writer.jsonFloat(((BackJSONFloat) part).value);
		} else if (part instanceof BackJSONTrue) {
			writer.jsonTrue();
		} else if (part instanceof BackJSONFalse) {
			writer.jsonFalse();
		} else if (part instanceof BackJSONNull) {
			writer.jsonNull();
		} else if (part instanceof BackType) {
			final BackType typePart = (BackType) part;
			writer.type(typePart.type);
			stack.addLast(new WriteStateBack(base, ImmutableList.of(typePart.value).iterator()));
		} else if (part instanceof BackArray) {
			writer.arrayBegin();
			stack.addLast(new WriteStateArrayEnd());
			stack.addLast(new WriteStateBack(base, ((BackArray) part).elements.iterator()));
		} else if (part instanceof BackRecord) {
			writer.recordBegin();
			stack.addLast(new WriteStateRecordEnd());
			stack.addLast(new WriteStateRecord(base, ((BackRecord) part).pairs));
		} else if (part instanceof BackDataType) {
			final BackDataType typePart = (BackDataType) part;
			writer.type(((ValuePrimitive) base.data.get(((BackDataType) part).type)).get());
			stack.addLast(new WriteStateBack(base, ImmutableList.of(typePart.value).iterator()));
		} else if (part instanceof BackDataPrimitive) {
			writer.primitive(((ValuePrimitive) base.data.get(((BackDataPrimitive) part).middle)).get());
		} else if (part instanceof BackDataJSONInt) {
			writer.jsonInt(((ValuePrimitive) base.data.get(((BackDataJSONInt) part).middle)).get());
		} else if (part instanceof BackDataJSONFloat) {
			writer.jsonFloat(((ValuePrimitive) base.data.get(((BackDataJSONFloat) part).middle)).get());
		} else if (part instanceof BackDataAtom) {
			final Atom child = ((ValueAtom) base.data.get(((BackDataAtom) part).middle)).data;
			stack.addLast(new WriteStateBack(child, child.type.back().iterator()));
		} else if (part instanceof BackDataArray) {
			writer.arrayBegin();
			stack.addLast(new WriteStateArrayEnd());
			stack.addLast(new WriteStateDataArray(((ValueArray) base.data.get(((BackDataArray) part).middle))));
		} else if (part instanceof BackDataRootArray) {
			stack.addLast(new WriteStateDataArray(((ValueArray) base.data.get(((BackDataRootArray) part).middle))));
		} else if (part instanceof BackDataRecord) {
			writer.recordBegin();
			stack.addLast(new WriteStateRecordEnd());
			stack.addLast(new WriteStateDataArray(((ValueArray) base.data.get(((BackDataRecord) part).middle))));
		} else if (part instanceof BackDataKey) {
			writer.key(((ValuePrimitive) base.data.get(((BackDataKey) part).middle)).get());
		} else
			throw new AssertionError(String.format("Unimplemented back part type [%s].\n",
					part.getClass().getCanonicalName()
			));
	}

	private static class WriteStateBack extends WriteState {
		private final Atom base;
		private final Iterator<BackPart> iterator;

		public WriteStateBack(final Atom base, final Iterator<BackPart> iterator) {
			this.base = base;
			this.iterator = iterator;
		}

		@Override
		public void run(final Deque<WriteState> stack, final EventConsumer writer) throws IOException {
			if (!iterator.hasNext()) {
				stack.removeLast();
				return;
			}
			writePart(stack, writer, base, iterator.next());
		}
	}

	private static class WriteStateRecord extends WriteState {
		private final Atom base;
		private final Iterator<Map.Entry<String, BackPart>> iterator;

		private WriteStateRecord(final Atom base, final Map<String, BackPart> record) throws IOException {
			this.base = base;
			this.iterator = record.entrySet().iterator();
		}

		@Override
		public void run(final Deque<WriteState> stack, final EventConsumer writer) throws IOException {
			if (!iterator.hasNext()) {
				stack.removeLast();
				return;
			}
			final Map.Entry<String, BackPart> next = iterator.next();
			writer.key(next.getKey());
			writePart(stack, writer, base, next.getValue());
		}
	}

	private static class WriteStateDataArray extends WriteState {
		private final Iterator<Atom> iterator;

		private WriteStateDataArray(final ValueArray array) throws IOException {
			this.iterator = array.data.iterator();
		}

		@Override
		public void run(final Deque<WriteState> stack, final EventConsumer writer) throws IOException {
			if (!iterator.hasNext()) {
				stack.removeLast();
				return;
			}
			final Atom next = iterator.next();
			stack.addLast(new WriteStateBack(next, next.type.back().iterator()));
		}
	}

	private static class WriteStateArrayEnd extends WriteState {

		@Override
		public void run(final Deque<WriteState> stack, final EventConsumer writer) throws IOException {
			writer.arrayEnd();
			stack.removeLast();
		}
	}

	private static class WriteStateRecordEnd extends WriteState {

		@Override
		public void run(final Deque<WriteState> stack, final EventConsumer writer) throws IOException {
			writer.recordEnd();
			stack.removeLast();
		}
	}

	public static void write(final Atom atom, final EventConsumer writer) {
		final Deque<WriteState> stack = new ArrayDeque<>();
		stack.addLast(new WriteStateBack(atom, atom.type.back().iterator()));
		uncheck(() -> {
			while (!stack.isEmpty())
				stack.getLast().run(stack, writer);
		});
	}
}
