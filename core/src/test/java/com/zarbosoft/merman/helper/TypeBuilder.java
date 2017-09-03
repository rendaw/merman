package com.zarbosoft.merman.helper;

import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.alignments.AbsoluteAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.ConcensusAlignmentDefinition;
import com.zarbosoft.merman.syntax.alignments.RelativeAlignmentDefinition;
import com.zarbosoft.merman.syntax.back.BackPart;
import com.zarbosoft.merman.syntax.front.*;
import com.zarbosoft.merman.syntax.middle.*;
import com.zarbosoft.merman.syntax.symbol.SymbolSpace;
import com.zarbosoft.merman.syntax.symbol.SymbolText;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeBuilder {
	private final FreeAtomType type;

	public TypeBuilder(final String id) {
		this.type = new FreeAtomType();
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

	public FreeAtomType build() {
		return type;
	}

	public TypeBuilder front(final FrontPart front) {
		type.front.add(front);
		return this;
	}

	public TypeBuilder autoComplete(final int x) {
		type.autoChooseAmbiguity = x;
		return this;
	}

	public TypeBuilder frontDataNode(final String middle) {
		final FrontDataAtom part = new FrontDataAtom();
		part.middle = middle;
		type.front.add(part);
		return this;
	}

	public TypeBuilder frontDataArray(final String middle) {
		final FrontDataArray part = new FrontDataArray();
		part.middle = middle;
		type.front.add(part);
		return this;
	}

	public TypeBuilder frontDataPrimitive(final String middle) {
		final FrontDataPrimitive part = new FrontDataPrimitive();
		part.middle = middle;
		type.front.add(part);
		return this;
	}

	public TypeBuilder frontMark(final String value) {
		final FrontSymbol part = new FrontSymbol();
		part.type = new SymbolText(value);
		type.front.add(part);
		return this;
	}

	public TypeBuilder frontSpace() {
		final FrontSymbol part = new FrontSymbol();
		part.type = new SymbolSpace();
		type.front.add(part);
		return this;
	}

	public TypeBuilder middlePrimitive(final String id) {
		final MiddlePrimitive middle = new MiddlePrimitive();
		middle.id = id;
		this.type.middle.put(id, middle);
		return this;
	}

	public TypeBuilder middleNode(final String id, final String type) {
		final MiddleAtom middle = new MiddleAtom();
		middle.type = type;
		middle.id = id;
		this.type.middle.put(id, middle);
		return this;
	}

	public TypeBuilder middleArray(final String id, final String type) {
		final MiddleArray middle = new MiddleArray();
		middle.type = type;
		middle.id = id;
		this.type.middle.put(id, middle);
		return this;
	}

	public TypeBuilder middleRecord(final String id, final String type) {
		final MiddleRecord middle = new MiddleRecord();
		middle.type = type;
		middle.id = id;
		this.type.middle.put(id, middle);
		return this;
	}

	public TypeBuilder middleRecordKey(final String id) {
		final MiddleRecordKey middle = new MiddleRecordKey();
		middle.id = id;
		this.type.middle.put(id, middle);
		return this;
	}

	public TypeBuilder precedence(final int precedence) {
		this.type.precedence = precedence;
		return this;
	}

	public TypeBuilder associateAfter() {
		this.type.frontAssociative = false;
		return this;
	}

	public TypeBuilder associateBefore() {
		this.type.frontAssociative = true;
		return this;
	}

	public TypeBuilder depthScore(final int i) {
		this.type.depthScore = i;
		return this;
	}

	public TypeBuilder absoluteAlignment(final String name, final int offset) {
		final AbsoluteAlignmentDefinition definition = new AbsoluteAlignmentDefinition();
		definition.offset = offset;
		this.type.alignments.put(name, definition);
		return this;
	}

	public TypeBuilder relativeAlignment(final String name, final String base, final int offset) {
		final RelativeAlignmentDefinition definition = new RelativeAlignmentDefinition();
		definition.offset = offset;
		definition.base = base;
		this.type.alignments.put(name, definition);
		return this;
	}

	public TypeBuilder concensusAlignment(final String name) {
		this.type.alignments.put(name, new ConcensusAlignmentDefinition());
		return this;
	}
}
