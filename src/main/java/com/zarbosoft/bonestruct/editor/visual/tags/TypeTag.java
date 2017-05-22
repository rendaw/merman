package com.zarbosoft.bonestruct.editor.visual.tags;

import com.zarbosoft.interface1.Configuration;

import java.util.Objects;

@Configuration(name = "type")
public class TypeTag implements Tag {
	@Configuration
	public String value;

	public TypeTag() {
	}

	public TypeTag(final String value) {
		this.value = value;
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof TypeTag && value.equals(((TypeTag) obj).value);
	}

	public String toString() {
		return String.format("type:%s", value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(TypeTag.class.hashCode(), value);
	}
}
