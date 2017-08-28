package com.zarbosoft.bonestruct.editor.visual.tags;

import com.zarbosoft.interface1.Configuration;

import java.util.Objects;

@Configuration(name = "part")
public class PartTag implements Tag {
	@Configuration
	public String value;

	public PartTag() {
	}

	public PartTag(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof PartTag && value.equals(((PartTag) obj).value);
	}

	public String toString() {
		return String.format("part:%s", value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(PartTag.class.hashCode(), value);
	}
}
