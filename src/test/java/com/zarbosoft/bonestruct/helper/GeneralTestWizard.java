package com.zarbosoft.bonestruct.helper;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.banner.BannerMessage;
import com.zarbosoft.bonestruct.editor.details.DetailsPage;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Drawing;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.Course;
import com.zarbosoft.bonestruct.editor.wall.bricks.BrickSpace;
import com.zarbosoft.bonestruct.editor.wall.bricks.BrickText;
import com.zarbosoft.bonestruct.syntax.Syntax;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.zarbosoft.rendaw.common.Common.iterable;
import static com.zarbosoft.rendaw.common.Common.zip;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.junit.Assert.assertThat;

public class GeneralTestWizard {
	TestWizard inner;

	public GeneralTestWizard(final Syntax syntax, final Atom... atoms) {
		inner = new TestWizard(syntax, atoms);
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

	public GeneralTestWizard checkTextBrick(final int courseIndex, final int brickIndex, final String text) {
		assertThat(inner.context.foreground.children.size(), greaterThan(courseIndex));
		final Course course = inner.context.foreground.children.get(courseIndex);
		assertThat(course.children.size(), greaterThan(brickIndex));
		final Brick brick = course.children.get(brickIndex);
		assertThat(brick, instanceOf(BrickText.class));
		assertThat(((BrickText) brick).text.text(), equalTo(text));
		return this;
	}

	public GeneralTestWizard checkSpaceBrick(final int courseIndex, final int brickIndex) {
		assertThat(inner.context.foreground.children.size(), greaterThan(courseIndex));
		final Course course = inner.context.foreground.children.get(courseIndex);
		assertThat(course.children.size(), greaterThan(brickIndex));
		final Brick brick = course.children.get(brickIndex);
		assertThat(brick, instanceOf(BrickSpace.class));
		return this;
	}

	public GeneralTestWizard run(final Consumer<Context> r) {
		r.accept(inner.context);
		assertThat(inner.context.selection, is(notNullValue()));
		inner.runner.flush();
		return this;
	}

	public GeneralTestWizard act(final String name) {
		for (final Action action : iterable(inner.context.actions
				.entrySet()
				.stream()
				.flatMap(e -> e.getValue().stream()))) {
			if (action.getName().equals(name)) {
				action.run(inner.context);
				assertThat(inner.context.selection, is(notNullValue()));
				inner.runner.flush();
				return this;
			}
		}
		throw new AssertionError(String.format("No action named [%s]", name));
	}

	public GeneralTestWizard checkCourseCount(final int i) {
		assertThat(inner.context.foreground.children.size(), equalTo(i));
		return this;
	}

	public GeneralTestWizard checkBrickCount(final int i) {
		assertThat(
				inner.context.foreground.children.stream().mapToInt(course -> course.children.size()).sum(),
				equalTo(i)
		);
		return this;
	}

	public GeneralTestWizard sendHIDEvent(final HIDEvent event) {
		inner.sendHIDEvent(event);
		return this;
	}

	public GeneralTestWizard checkTree(final Atom... atoms) {
		zip(Stream.of(atoms), inner.context.document.top.data.stream()).forEach(pair -> {
			Helper.assertTreeEqual(pair.first, pair.second);
		});
		return this;
	}
}
