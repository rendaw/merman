package com.zarbosoft.merman;

import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

public class TestPrimitivePatterns {
	public final static FreeAtomType unquoted;
	public final static FreeAtomType quoted;
	public final static Syntax syntax;

	static {
		unquoted = new TypeBuilder("unquoted")
				.middlePrimitiveLetters("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontDataPrimitive("value")
				.build();
		quoted = new TypeBuilder("quoted")
				.middlePrimitiveLetters("value")
				.back(Helper.buildBackDataPrimitive("value"))
				.frontMark("\"")
				.frontDataPrimitive("value")
				.frontMark("\"")
				.build();
		syntax = new SyntaxBuilder("any")
				.type(unquoted)
				.type(quoted)
				.group("any", new GroupBuilder().type(unquoted).type(quoted).build())
				.build();
		syntax.retryExpandFactor = 1.05;
	}

	@Test
	public void testQuotedAllowed() {
		final Atom atom = new TreeBuilder(quoted).add("value", "").build();
		new GeneralTestWizard(syntax, atom)
				.run(context -> atom.data.get("value").selectDown(context))
				.sendText("a")
				.checkArrayTree(new TreeBuilder(quoted).add("value", "a").build());
	}

	@Test
	public void testQuotedDisallowed() {
		final Atom atom = new TreeBuilder(quoted).add("value", "").build();
		new GeneralTestWizard(syntax, atom)
				.run(context -> atom.data.get("value").selectDown(context))
				.sendText("1")
				.checkArrayTree(new TreeBuilder(quoted).add("value", "").build());
	}

	@Test
	public void testDisallowedSuffix() {
		final Atom atom = new TreeBuilder(unquoted).add("value", "").build();
		new GeneralTestWizard(syntax, atom)
				.run(context -> atom.data.get("value").selectDown(context))
				.sendText("1")
				.checkArrayTree(new TreeBuilder(syntax.suffixGap)
						.addArray("value", new TreeBuilder(unquoted).add("value", "").build())
						.add("gap", "1")
						.build());
	}
}
