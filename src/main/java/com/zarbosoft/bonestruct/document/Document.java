package com.zarbosoft.bonestruct.document;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.luxem.write.RawWriter;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
				write(top, new RawWriter(stream, (byte) ' ', 4));
				stream.write('\n');
				stream.flush();
			}
		});
	}

	public static void write(final Value value, final RawWriter writer) {
		uncheck(() -> {
			if (value.getClass() == ValueArray.class) {
				writer.arrayBegin();
				((ValueArray) value).get().stream().forEach(element -> write(element, writer));
				writer.arrayEnd();
			} else if (value.getClass() == ValueNode.class) {
				write(((ValueNode) value).get(), writer);
			} else if (value.getClass() == ValuePrimitive.class) {
				writer.quotedPrimitive(((ValuePrimitive) value).get().getBytes(StandardCharsets.UTF_8));
			} else
				throw new DeadCode();
		});
	}

	private static void write(final Node value, final RawWriter writer) {
		uncheck(() -> {
			writer.type(value.type.id.getBytes(StandardCharsets.UTF_8));
			writer.recordBegin();
			value.data
					.keySet()
					.forEach(k -> write(value.data.get(k),
							uncheck(() -> writer.key(k.getBytes(StandardCharsets.UTF_8)))
					));
			writer.recordEnd();
		});
	}
}
