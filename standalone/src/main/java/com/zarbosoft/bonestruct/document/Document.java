package com.zarbosoft.bonestruct.document;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueAtom;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.document.values.ValueRecordKey;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.back.*;
import com.zarbosoft.luxem.write.RawWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Document {

	final public Syntax syntax;
	final public Atom root;

	public Document(final Syntax syntax, final Atom root) {
		this.syntax = syntax;
		this.root = root;
	}

	public void write(final Path out) {
		uncheck(() -> {
			try (OutputStream stream = Files.newOutputStream(out)) {
				write(stream);
			}
		});
	}

	public void write(final OutputStream stream) {
		uncheck(() -> {
			final RawWriter writer = syntax.prettySave ? new RawWriter(stream, (byte) ' ', 4) : new RawWriter(stream);
			write(root, writer);
			if (syntax.prettySave)
				stream.write('\n');
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
		public abstract void run(Deque<WriteState> stack, RawWriter writer) throws IOException;
	}

	private static void writePart(
			final Deque<WriteState> stack, final RawWriter writer, final Atom base, final BackPart part
	) throws IOException {
		if (part instanceof BackPrimitive) {
			writer.primitive(((BackPrimitive) part).value);
		} else if (part instanceof BackType) {
			final BackType typePart = (BackType) part;
			writer.type(typePart.value);
			stack.addLast(new WriteStateBack(base, ImmutableList.of(typePart.child).iterator()));
		} else if (part instanceof BackArray) {
			writer.arrayBegin();
			stack.addLast(new WriteStateArrayEnd());
			stack.addLast(new WriteStateBack(base, ((BackArray) part).elements.iterator()));
		} else if (part instanceof BackRecord) {
			writer.recordBegin();
			stack.addLast(new WriteStateRecordEnd());
			stack.addLast(new WriteStateRecord(base, ((BackRecord) part).pairs));
		} else if (part instanceof BackDataPrimitive) {
			writer.primitive(((ValuePrimitive) base.data.get(((BackDataPrimitive) part).middle)).get());
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
			writer.key(((ValueRecordKey) base.data.get(((BackDataKey) part).middle)).get());
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
		public void run(final Deque<WriteState> stack, final RawWriter writer) throws IOException {
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
		public void run(final Deque<WriteState> stack, final RawWriter writer) throws IOException {
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
		public void run(final Deque<WriteState> stack, final RawWriter writer) throws IOException {
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
		public void run(final Deque<WriteState> stack, final RawWriter writer) throws IOException {
			writer.arrayEnd();
			stack.removeLast();
		}
	}

	private static class WriteStateRecordEnd extends WriteState {

		@Override
		public void run(final Deque<WriteState> stack, final RawWriter writer) throws IOException {
			writer.recordEnd();
			stack.removeLast();
		}
	}

	public static void write(final Atom atom, final RawWriter writer) {
		final Deque<WriteState> stack = new ArrayDeque<>();
		stack.addLast(new WriteStateBack(atom, atom.type.back().iterator()));
		uncheck(() -> {
			while (!stack.isEmpty())
				stack.getLast().run(stack, writer);
		});
	}
}
