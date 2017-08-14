package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class PrimitiveTestWizard {
	TestWizard inner;
	private final VisualPrimitive primitive;

	public PrimitiveTestWizard(final String string) {
		inner = new TestWizard(PrimitiveSyntax.syntax,
				new TreeBuilder(PrimitiveSyntax.primitive).add("value", string).build()
		);
		this.primitive =
				((ValuePrimitive) Helper.rootArray(inner.context.document).data.get(0).data.get("value")).visual;
	}

	public PrimitiveTestWizard check(final String... lines) {
		assertThat(primitive.lines.stream().map(line -> line.text).toArray(), equalTo(lines));
		return this;
	}

	public PrimitiveTestWizard resize(final int size) {
		inner.resize(size);
		return this;
	}

	public PrimitiveTestWizard resizeTransitive(final int size) {
		inner.resizeTransitive(size);
		return this;
	}
}
