package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.back.BackPrimitive;
import com.zarbosoft.bonestruct.editor.model.front.FrontMark;
import com.zarbosoft.bonestruct.editor.model.front.FrontPart;
import com.zarbosoft.bonestruct.editor.model.middle.DataArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Builders {
	static class SyntaxBuilder {

		private final Syntax syntax;

		public SyntaxBuilder(final String root) {
			this.syntax = new Syntax();
			syntax.root = new DataArray();
			syntax.root.type = root;
		}

		public SyntaxBuilder type(final FreeNodeType type) {
			syntax.types.add(type);
			return this;
		}

		public Syntax build() {
			syntax.finish();
			return syntax;
		}

		public SyntaxBuilder group(final String name, final Set<String> subtypes) {
			syntax.groups.put(name, subtypes);
			return this;
		}
	}

	static class GroupBuilder {
		Set<String> subtypes = new HashSet<>();

		public GroupBuilder type(final FreeNodeType type) {
			subtypes.add(type.id);
			return this;
		}

		public Set<String> build() {
			return subtypes;
		}
	}

	static class TypeBuilder {
		private final FreeNodeType type;

		public TypeBuilder(final String id) {
			this.type = new FreeNodeType();
			type.id = id;
			type.name = id;
			type.back = new ArrayList<>();
			type.middle = new HashMap<>();
			type.front = new ArrayList<>();
		}

		public TypeBuilder back(final BackPart back) {
			type.back.add(back);
			return this;
		}

		public FreeNodeType build() {
			return type;
		}

		public TypeBuilder front(final FrontPart front) {
			type.front.add(front);
			return this;
		}

		public TypeBuilder immediate() {
			type.immediateMatch = true;
			return this;
		}
	}

	static class BackPrimitiveBuilder {
		BackPrimitive back = new BackPrimitive();

		public BackPrimitiveBuilder value(final String value) {
			back.value = value;
			return this;
		}

		public BackPart build() {
			return back;
		}
	}

	public static class FrontMarkBuilder {
		private final FrontMark front;

		public FrontMarkBuilder(final String value) {
			this.front = new FrontMark();
			front.value = value;
		}

		public FrontMark build() {
			return front;
		}
	}
}
