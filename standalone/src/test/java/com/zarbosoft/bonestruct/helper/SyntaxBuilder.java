package com.zarbosoft.bonestruct.helper;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.RootAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.front.FrontDataArray;
import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.style.Style;

import java.util.Set;

public class SyntaxBuilder {

	private final Syntax syntax;

	public SyntaxBuilder(final String root) {
		this.syntax = new Syntax();
		syntax.root = new RootAtomType();
		syntax.root.back = ImmutableList.of(Helper.buildBackDataRootArray("value"));
		syntax.root.front = ImmutableList.of(new FrontDataArrayBuilder("value").build());
		final MiddleArray middle = new MiddleArray();
		middle.id = "value";
		middle.type = root;
		syntax.root.middle.put("value", middle);
	}

	public SyntaxBuilder type(final FreeAtomType type) {
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

	public SyntaxBuilder style(final Style style) {
		syntax.styles.add(style);
		return this;
	}

	public SyntaxBuilder absoluteAlignment(final String name, final int offset) {
		final AbsoluteAlignmentDefinition definition = new AbsoluteAlignmentDefinition();
		definition.offset = offset;
		this.syntax.root.alignments.put(name, definition);
		return this;
	}

	public SyntaxBuilder relativeAlignment(final String name, final int offset) {
		final RelativeAlignmentDefinition definition = new RelativeAlignmentDefinition();
		definition.offset = offset;
		this.syntax.root.alignments.put(name, definition);
		return this;
	}

	public SyntaxBuilder concensusAlignment(final String name) {
		this.syntax.root.alignments.put(name, new ConcensusAlignmentDefinition());
		return this;
	}

	public SyntaxBuilder addRootFrontSeparator(final FrontSymbol part) {
		((FrontDataArray) syntax.root.front.get(0)).separator.add(part);
		return this;
	}

	public SyntaxBuilder addRootFrontPrefix(final FrontSymbol part) {
		((FrontDataArray) syntax.root.front.get(0)).prefix.add(part);
		return this;
	}
}
