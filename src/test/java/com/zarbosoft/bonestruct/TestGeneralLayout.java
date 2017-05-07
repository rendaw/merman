package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.banner.BannerMessage;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Drawing;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.wall.Course;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.StyleBuilder;
import com.zarbosoft.bonestruct.helper.TestWizard;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

public class TestGeneralLayout {
	final static FreeNodeType one;
	final static FreeNodeType two;
	final static Syntax syntax;

	static {
		one = new Helper.TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new Helper.FrontMarkBuilder("one").build())
				.build();
		two = new Helper.TypeBuilder("two")
				.back(Helper.buildBackPrimitive("two"))
				.front(new Helper.FrontMarkBuilder("two").build())
				.build();
		syntax = new Helper.SyntaxBuilder("any")
				.type(one)
				.type(two)
				.group("any", new Helper.GroupBuilder().type(one).type(two).build())
				.style(new StyleBuilder().broken().build())
				.build();
	}

	public static class GeneralTestWizard {
		TestWizard inner;

		public GeneralTestWizard(final Syntax syntax, final Node... nodes) {
			inner = new TestWizard(syntax, nodes);
			inner.context.banner.addMessage(inner.context, new BannerMessage() {

			});
			final Drawing drawing = inner.context.display.drawing();
			drawing.resize(inner.context, new Vector(500, 7));
			final DetailsPage page = new DetailsPage() {
				@Override
				public void tagsChanged(final Context context) {

				}
			};
			page.node = drawing;
			inner.context.details.addPage(inner.context, page);
			inner.runner.flush();
		}

		public GeneralTestWizard resize(final int size) {
			inner.resize(size);
			return this;
		}

		public GeneralTestWizard resizeTransitive(final int size) {
			inner.resizeTransitive(size);
			return this;
		}

		private void checkNode(final DisplayNode node, final int c, final int t, final int ce, final int te) {
			assertThat(node.converse(inner.context), equalTo(c));
			assertThat(node.transverse(inner.context), equalTo(t));
			assertThat(node.converseEdge(inner.context), equalTo(ce));
			assertThat(node.transverseEdge(inner.context), equalTo(te));
		}

		private void checkNode(final DisplayNode node, final int t, final int te) {
			assertThat(node.transverse(inner.context), equalTo(t));
			assertThat(node.transverseEdge(inner.context), equalTo(te));
		}

		public GeneralTestWizard checkBanner(final int t, final int te) {
			checkNode(inner.context.banner.text, t, te);
			return this;
		}

		public GeneralTestWizard checkDetails(final int t, final int te) {
			checkNode(inner.context.details.current.node, t, te);
			return this;
		}

		public GeneralTestWizard checkScroll(final int scroll) {
			assertThat(inner.context.scroll, equalTo(scroll));
			return this;
		}

		public GeneralTestWizard checkCourse(final int index, final int t, final int te) {
			assertThat(inner.context.foreground.children.size(), greaterThan(index));
			final Course course = inner.context.foreground.children.get(index);
			assertThat(course.transverseStart, equalTo(t));
			assertThat(course.transverseStart + course.ascent + course.descent, equalTo(te));
			return this;
		}

		public GeneralTestWizard run(final Consumer<Context> r) {
			r.accept(inner.context);
			inner.runner.flush();
			return this;
		}
	}

	@Test
	public void testInitialLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		).checkScroll(-10).checkCourse(0, 0, 10).checkCourse(1, 17, 27).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testClippedLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		).resizeTransitive(40).checkScroll(-10).checkBanner(8, 10).checkDetails(20, 27);
	}

	@Test
	public void testScrollLayout() {
		new GeneralTestWizard(
				syntax,
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(two).build(),
				new Helper.TreeBuilder(one).build(),
				new Helper.TreeBuilder(one).build()
		)
				.resizeTransitive(40)
				.run(context -> {
					context.document.top.get().get(4).getVisual().selectUp(context);
				})
				.checkScroll(24)
				.checkCourse(4, 47, 57)
				.checkCourse(3, 27, 37)
				.checkCourse(5, 64, 74)
				.checkBanner(21, 23)
				.checkDetails(33, 40);
	}
}
