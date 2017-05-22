package com.zarbosoft.bonestruct.editor.visual.tags;

import com.zarbosoft.interface1.Configuration;

import java.util.Objects;

@Configuration(name = "free")
public class FreeTag implements Tag {
	@Configuration
	public String value;

	public FreeTag() {
	}

	public FreeTag(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof FreeTag && value.equals(((FreeTag) obj).value);
	}

	public String toString() {
		return String.format("free:%s", value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(FreeTag.class.hashCode(), value);
	}
}
