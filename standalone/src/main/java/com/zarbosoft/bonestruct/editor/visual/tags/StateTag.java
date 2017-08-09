package com.zarbosoft.bonestruct.editor.visual.tags;

import com.zarbosoft.interface1.Configuration;

import java.util.Objects;

@Configuration(name = "state")
public class StateTag implements Tag {
	@Configuration
	public String value;

	public StateTag() {
	}

	public StateTag(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof StateTag && value.equals(((StateTag) obj).value);
	}

	public String toString() {
		return String.format("state:%s", value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(StateTag.class.hashCode(), value);
	}
}
