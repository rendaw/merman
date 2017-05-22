package com.zarbosoft.bonestruct.editor.visual.tags;

import com.zarbosoft.interface1.Configuration;

import java.util.Objects;

@Configuration(name = "global")
public class GlobalTag implements Tag {
	@Configuration
	public String value;

	public GlobalTag() {
	}

	public GlobalTag(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof GlobalTag && value.equals(((GlobalTag) obj).value);
	}

	public String toString() {
		return String.format("global:%s", value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(GlobalTag.class.hashCode(), value);
	}
}
