package com.zarbosoft.merman.editor.serialization.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.zarbosoft.merman.editor.backevents.*;
import com.zarbosoft.merman.editor.serialization.json.path.JSONObjectPath;
import com.zarbosoft.merman.editor.serialization.json.path.JSONPath;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.BaseParse;
import com.zarbosoft.pidgoon.internal.Callback;
import com.zarbosoft.rendaw.common.DeadCode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class JSONParse<O> extends BaseParse<JSONParse<O>> {

	private int eventUncertainty = 20;

	private JSONParse(final JSONParse<O> other) {
		super(other);
		this.eventUncertainty = other.eventUncertainty;
	}

	@Override
	protected JSONParse<O> split() {
		return new JSONParse<>(this);
	}

	public JSONParse() {
	}

	public JSONParse<O> eventUncertainty(final int limit) {
		if (eventUncertainty != 20)
			throw new IllegalArgumentException("Max event uncertainty already set");
		final JSONParse<O> out = split();
		out.eventUncertainty = limit;
		return out;
	}

	public O parse(final String string) {
		return parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	public O parse(final InputStream stream) {
		return uncheck(() -> {
			EventStream<O> eventStream = new com.zarbosoft.pidgoon.events.Parse<O>()
					.grammar(grammar)
					.root(root)
					.stack(initialStack)
					.errorHistory(errorHistoryLimit)
					.dumpAmbiguity(dumpAmbiguity)
					.uncertainty(eventUncertainty)
					.callbacks((Map<Object, Callback<Store>>) (Object) callbacks)
					.parse();
			JSONPath path = new JSONObjectPath(null);
			final JsonParser stream1 = new JsonFactory().createParser(stream);
			while (true) {
				final JsonToken token = stream1.nextToken();
				if (token == null)
					break;
				switch (token) {
					case NOT_AVAILABLE:
						// Only async mode
						throw new DeadCode();
					case START_OBJECT: {
						final BackEvent e = new EObjectOpenEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case END_OBJECT: {
						final BackEvent e = new EObjectCloseEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case START_ARRAY: {
						final BackEvent e = new EArrayOpenEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case END_ARRAY: {
						final BackEvent e = new EArrayCloseEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case FIELD_NAME: {
						final BackEvent e = new EKeyEvent(token.asString());
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_EMBEDDED_OBJECT:
						// Supposedly shouldn't apply with normal options
						throw new DeadCode();
					case VALUE_STRING: {
						final BackEvent e = new EPrimitiveEvent(token.asString());
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_NUMBER_INT: {
						final BackEvent e = new JIntEvent(token.asString());
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_NUMBER_FLOAT: {
						final BackEvent e = new JFloatEvent(token.asString());
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_TRUE: {
						final BackEvent e = new JTrueEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_FALSE: {
						final BackEvent e = new JFalseEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					case VALUE_NULL: {
						final BackEvent e = new JNullEvent();
						eventStream = eventStream.push(e, path.toString());
						path = path.push(e);
						break;
					}
					default:
						throw new DeadCode();
				}
			}
			return eventStream.finish();
		});
	}

}
