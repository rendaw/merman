package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.front.FrontSymbol;
import com.zarbosoft.bonestruct.syntax.middle.MiddleArray;
import com.zarbosoft.bonestruct.syntax.style.Style;

import java.util.Set;

public class SyntaxBuilder {

	private final Syntax syntax;

	public SyntaxBuilder(final String root) {
		this.syntax = new Syntax();
		syntax.root = new MiddleArray();
		syntax.root.type = root;
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
		this.syntax.rootAlignments.put(name, definition);
		return this;
	}

	public SyntaxBuilder relativeAlignment(final String name, final int offset) {
		final RelativeAlignmentDefinition definition = new RelativeAlignmentDefinition();
		definition.offset = offset;
		this.syntax.rootAlignments.put(name, definition);
		return this;
	}

	public SyntaxBuilder concensusAlignment(final String name) {
		this.syntax.rootAlignments.put(name, new ConcensusAlignmentDefinition());
		return this;
	}

	public SyntaxBuilder addRootFrontSeparator(final FrontSymbol part) {
		syntax.rootFront.separator.add(part);
		return this;
	}

	public SyntaxBuilder addRootFrontPrefix(final FrontSymbol part) {
		syntax.rootFront.prefix.add(part);
		return this;
	}
}
