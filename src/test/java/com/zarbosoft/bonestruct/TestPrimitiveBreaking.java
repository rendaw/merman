package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.display.MockeryDisplay;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.IdleTask;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualPrimitive;
import org.junit.Test;

import java.util.PriorityQueue;

import static com.zarbosoft.bonestruct.Helper.buildDoc;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestPrimitiveBreaking {
	static class IdleRunner {
		private final PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();

		public void idleAdd(final IdleTask task) {
			idleQueue.add(task);
		}

		public void flush() {
			for (int i = 0; i < 1000; ++i) { // Batch
				final IdleTask top = idleQueue.poll();
				if (top == null) {
					return;
				} else {
					top.run();
				}
			}
			throw new AssertionError("Too much idle activity");
		}
	}

	static class TestWizard {
		private final IdleRunner runner;
		private final Context context;
		private final VisualPrimitive primitive;
		private final MockeryDisplay display;

		TestWizard(final String string) {
			this.runner = new IdleRunner();
			this.context = buildDoc(
					runner::idleAdd,
					PrimitiveSyntax.syntax,
					new Helper.TreeBuilder(PrimitiveSyntax.primitive).add("value", string).build()
			);
			this.primitive = (VisualPrimitive) context.document.top.get().get(0).data.get("value").getVisual();
			this.display = (MockeryDisplay) context.display;
			runner.flush();
		}

		public TestWizard check(final String... lines) {
			assertThat(primitive.lines.stream().map(line -> line.text).toArray(), equalTo(lines));
			return this;
		}

		public TestWizard resize(final int size) {
			display.setConverseEdge(context, size);
			runner.flush();
			return this;
		}
	}

	@Test
	public void testHardLines() {
		new TestWizard("amp dog\npear").check("amp dog", "pear");
	}

	@Test
	public void testNoBreakUnbreak() {
		new TestWizard("amp dog").resize(3000).check("amp dog").resize(2000000).check("amp dog");
	}

	@Test
	public void testBreakOne() {
		new TestWizard("amp\npear digitize").resize(100).check("amp", "pear ", "digitize");
	}

	@Test
	public void testBreakTwo() {
		new TestWizard("amp dog laserasticatellage\npear volume")
				.resize(200)
				.check("amp dog ", "laserasticatellage", "pear volume");
	}

	@Test
	public void testRebreakOne() {
		new TestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(120)
				.check("over three ", "houses ", "rotisserie ", "volume");
	}

	@Test
	public void testRebreakTwo() {
		new TestWizard("over three houses timing\n rotisserie volume")
				.resize(200)
				.check("over three houses ", "timing", " rotisserie volume")
				.resize(160)
				.check("over three ", "houses timing", " rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOne() {
		new TestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(300)
				.check("over three houses rotisserie ", "volume");
	}

	@Test
	public void testUnbreakOneFully() {
		new TestWizard("over three houses rotisserie volume")
				.resize(200)
				.check("over three houses ", "rotisserie volume")
				.resize(1000)
				.check("over three houses rotisserie volume");
	}

	@Test
	public void testUnbreakableOne() {
		new TestWizard("123456789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableRebreak() {
		new TestWizard("123456789").resize(60).check("123456", "789").resize(40).check("1234", "5678", "9");
	}

	@Test
	public void testUnbreakableUnbreak() {
		new TestWizard("123456789").resize(40).check("1234", "5678", "9").resize(50).check("12345", "6789");
	}

	@Test
	public void testUnbreakableUnbreakFull() {
		new TestWizard("123456789").resize(40).check("1234", "5678", "9").resize(50).check("12345", "6789");
	}
}
