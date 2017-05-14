package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.PrimitiveSyntax;
import com.zarbosoft.bonestruct.helper.TestWizard;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestPrimitiveBreaking {

	@Test
	public void testHardLines() {
		new PrimitiveTestWizard("amp dog\npear").check("amp dog", "pear");
	}

	@Test
	public void testNoBreakUnbreak() {
		new PrimitiveTestWizard("amp dog").resize(3000).check("amp dog").resize(2000000).check("amp dog");
	}

	@Test
	public void testBreakOne() {
		new PrimitiveTestWizard("amp\npear digitize").resize(100).check("amp", "pear ", "digitize");
	}

	@Test
	public void testBreakTwo() {
		new PrimitiveTestWizard("amp dog laserasticatellage\npear volume")
				.resize(200)
				.check("amp dog ", "laserasticatellage", "pear volume");
	}

	@Test
	public void testRebreakOne() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(120)
				.check("over three ", "houses ", "rotisserie ", "volume");
	}

	@Test
	public void testRebreakTwo() {
		new PrimitiveTestWizard("over three houses timing\n rotisserie volume")
				.resize(200)
				.check("over three houses ", "timing", " rotisserie volume")
				.resize(160)
				.check("over three ", "houses timing", " rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOne() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(300)
				.check("over three houses rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOneFully() {
		new PrimitiveTestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(1000)
				.check("over three houses rotisserie volume");
	}

	@Test
	public void testUnbreakableOne() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableRebreak() {
		new PrimitiveTestWizard("123456789").resize(60).check("123456", "789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableUnbreak() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9").resize(50).check("12345", "6789");
	}

	@Test
	public void testUnbreakableUnbreakFull() {
		new PrimitiveTestWizard("123456789").resize(40).check("1234", "5678", "9").resize(50).check("12345", "6789");
	}

	public static class PrimitiveTestWizard {
		TestWizard inner;
		private final VisualPrimitive primitive;

		public PrimitiveTestWizard(final String string) {
			inner = new TestWizard(
					PrimitiveSyntax.syntax,
					new Helper.TreeBuilder(PrimitiveSyntax.primitive).add("value", string).build()
			);
			this.primitive = (VisualPrimitive) inner.context.document.top.data.get(0).data.get("value").getVisual();
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
}
