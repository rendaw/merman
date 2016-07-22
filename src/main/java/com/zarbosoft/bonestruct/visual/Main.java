package com.zarbosoft.bonestruct.visual;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Document;
import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.animation.Interpolator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class Main extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	class Context {
		private final Syntax luxemSyntax;

		public Context() {
			Luxem.grammar(); // Make sure the luxem grammar is loaded so the new resource stream doesn't get closed
			try (
					InputStream stream = Thread
							.currentThread()
							.getContextClassLoader()
							.getResourceAsStream("luxem.syntax")
			) {
				this.luxemSyntax = Syntax.loadSyntax(stream);
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	ScheduledFuture<?> idleTimer = null;
	boolean idlePending = false;

	public void idleStart() {
		if (idleTimer != null)
			return;
		idleTimer = worker.scheduleWithFixedDelay(() -> {
			if (idlePending)
				return;
			Platform.runLater(() -> {
				if (!idle())
					idleTimer.cancel(false);
				idlePending = false;
			});
		}, 500, 500, TimeUnit.MILLISECONDS);
	}

	abstract class IdleTask implements Comparable<IdleTask> {
		int priority() {
			return 0;
		}

		abstract void run();

		@Override
		public int compareTo(final IdleTask t) {
			return priority() - t.priority();
		}
	}

	PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();
	Compact idleCompact = null;

	public class TheInterpolator extends Interpolator {
		@Override
		protected double curve(double t) {
			t = t * 2;
			if (t * 2 < 1)
				return Math.pow(t, 3) / 2;
			else
				return Math.pow(t - 1, 3) / 2 + 1;
		}
	}

	@Override
	public void start(final Stage primaryStage) {
		// 1. create blank document + wrap via size changes
		// 2. navigation
		// 3. editing
		// 4. rewrap on edit, replace
		final Context context = new Context();
		final Document doc = context.luxemSyntax.load("[{x: 47,y:{ar:[2,9,13]},},[atler]]");
		final VBox layout = new VBox();
		final VisualNode root = doc.root.createVisual();
		depthFirst(root, n -> n.children(), n -> n.layoutInitial());
		root.offset(new Point2D(0, 50));
		idleCompact = new Compact(root, 300);
		idleStart();
		layout.getChildren().add(root.visual());
		final Scene scene = new Scene(layout, 300, 275);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public <T> void depthFirst(final T root, final Function<T, Iterator<T>> descend, final Consumer<T> consume) {
		final Deque<Pair<T, Iterator<T>>> stack = new ArrayDeque<>();
		stack.add(new Pair<>(root, Iterators.forArray(root)));
		while (!stack.isEmpty()) {
			final Pair<T, Iterator<T>> top = stack.getLast();
			if (top.second.hasNext()) {
				final T next = top.second.next();
				stack.addLast(new Pair<>(next, descend.apply(next)));
			} else {
				consume.accept(stack.getLast().first);
				stack.removeLast();
			}
		}
	}

	private boolean idle() {
		if (idleCompact != null)
			idleCompact.run();
		else {
			final IdleTask top = idleQueue.poll();
			if (top == null)
				return false;
			top.run();
		}
		return true;
	}

	class CompactionTreeNode implements Comparable<CompactionTreeNode> {
		boolean expanded;
		int priority;
		VisualNode node;
		PriorityQueue<CompactionTreeNode> children;

		public CompactionTreeNode(final VisualNode node) {
			this.node = node;
			priority = node.treeCompactPriority();
			expanded = false;
			children = new PriorityQueue<>();
		}

		@Override
		public int compareTo(final CompactionTreeNode t) {
			return priority - t.priority;
		}
	}

	private class Compact {
		private final CompactionTreeNode tree;
		private final int converseEdge;

		public Compact(final VisualNode root, final int converseEdge) {
			tree = new CompactionTreeNode(root);
			this.converseEdge = converseEdge;
		}

		void run() {
			if (tree.node.converseEdge() <= converseEdge) {
				idleCompact = null;
				return;
			}
			final Deque<CompactionTreeNode> walkStack = new ArrayDeque<>();
			CompactionTreeNode at = tree;

			// Walk tree taking high priority branches to find highest priority node
			while (true) {
				// Expand the tree if not yet walked
				if (!at.expanded) {
					final CompactionTreeNode tempAt = at;
					Helper
							.stream(at.node.children())
							.forEach(child -> tempAt.children.add(new CompactionTreeNode(child)));
					at.expanded = true;
				}
				if (at.children.isEmpty() || (at.children.peek().priority <= at.node.compactPriority()))
					break;
				walkStack.addLast(at);
				at = at.children.peek();
			}

			// Compact node
			at.compact(converseEdge);

			// Remove from tree
			if (walkStack.isEmpty()) {
				idleCompact = null;
			} else {
				walkStack.getLast().children.remove(at);
				walkStack.getLast().children.addAll(at.children);
			}

			// Recalculate priorities up tree
			while (!walkStack.isEmpty()) {
				at = walkStack.poll();
				at.priority = Stream
						.concat(Stream.of(at.node.compactPriority()), at.children.stream().map(n -> n.priority))
						.mapToInt(p -> p)
						.max();
				if (!walkStack.isEmpty()) {
					walkStack.getLast().children.remove(at);
					walkStack.getLast().children.add(at);
				}
			}
		}
	}
}
