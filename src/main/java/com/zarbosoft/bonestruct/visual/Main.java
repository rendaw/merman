package com.zarbosoft.bonestruct.visual;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Document;
import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.pidgoon.internal.Pair;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

public class Main extends Application {
	private IdleTask idleCompact = null;

	public static void main(final String[] args) {
		launch(args);
	}

	ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	boolean idlePending = false;
	ScheduledFuture<?> idleTimer = null;
	public PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();

	/*
	One window, one file only.  Cannot load, just save.
	TODO hooks
	metadata file changes - monitor and load
	save -> whole file
	source change -> diff
	primitive -> autocomplete command (context, metadata, produce japanese, plugin?)
	 */
	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent t) {
				worker.shutdown();
			}
		});
		// 1. create blank document + wrap via size changes
		// 2. navigation
		// 3. editing
		// 4. rewrap on edit, replace
		Luxem.grammar(); // Make sure the luxem grammar is loaded beforehand so the new resource stream doesn't get closed by that resource stream
		final Syntax luxemSyntax;
		try (
				InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("luxem.syntax")
		) {
			luxemSyntax = Syntax.loadSyntax(stream);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
		//final Document doc = luxemSyntax.load("[dog, dog, dog, dog, dog],");
		final Document doc = luxemSyntax.load("[{converse: 47,transverse:{ar:[2,9,13]},},[atler]]");
		//final Document doc = luxemSyntax.load("{analogue:bolivar}");
		final Context context = new Context(luxemSyntax, doc, this::addIdle);
		final VBox layout = new VBox();
		final VisualNode root = doc.root.createVisual(context);
		/*
		final VisualNode root;
		{
			// Test
			final GroupVisualNode x = new GroupVisualNode() {
				@Override
				public Break breakMode() {
					return Break.NEVER;
				}

				@Override
				public String alignmentName() {
					return null;
				}

				@Override
				public String alignmentNameCompact() {
					return null;
				}
			};
			final FrontMark a = new FrontMark();
			a.value = "{";
			x.add(context, a.createVisual(context));
			final FrontMark b = new FrontMark();
			b.value = "}";
			x.add(context, b.createVisual(context));
			root = x;
		}
		*/
		// TODO walk tree and resolve relative alignments
		final ScrollPane scroll = new ScrollPane();
		final StackPane stack = new StackPane();
		stack.getChildren().add(root.visual().background);
		stack.getChildren().add(root.visual().foreground);
		scroll.setContent(stack);
		layout.getChildren().add(scroll);
		final Scene scene = new Scene(layout, 300, 275);
		scene.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (context.hoverIdle != null) {
					context.hoverIdle.point = null;
				} else if (context.hover != null) {
					context.hover.clear(context);
					context.hover = null;
				}
			}
		});
		scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (context.hoverIdle == null) {
					context.hoverIdle = context.new HoverIdle(context, root);
					addIdle(context.hoverIdle);
				}
				context.hoverIdle.point = context.sceneToVector(scene, event.getX(), event.getY());
				/*
				System.out.format(
						"mouse x %f y %f c %d t %d\n",
						event.getX(),
						event.getY(),
						context.hoverIdle.point.converse,
						context.hoverIdle.point.transverse
				);
				*/
			}
		});
		final ChangeListener<Number> converseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			final int oldValue2 = (int) oldValue.doubleValue();
			//System.out.format("conv window size change: %d to %d\n", oldValue2, newValue2);
			context.edge = newValue2;
			if (newValue2 < oldValue2) {
				compact(context, doc.syntax.compactionMode, root);
			} else if (newValue2 > oldValue2) {
				// TODO expand
			}
		};
		final ChangeListener<Number> transverseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			context.transverseEdge = newValue2;
		};
		switch (doc.syntax.converseDirection) {
			case UP:
			case DOWN:
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				scene.heightProperty().addListener(converseSizeListener);
				//converseSizeListener.changed(null, Double.MAX_VALUE, scene.heightProperty().getValue());
				scene.widthProperty().addListener(transverseSizeListener);
				break;
			case LEFT:
			case RIGHT:
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				scene.widthProperty().addListener(converseSizeListener);
				scene.heightProperty().addListener(transverseSizeListener);
				break;
		}
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void addIdle(final IdleTask task) {
		idleQueue.add(task);
		if (idleTimer == null) {
			idleTimer = worker.scheduleWithFixedDelay(() -> {
				//System.out.println("idle timer");
				if (idlePending)
					return;
				idlePending = true;
				Platform.runLater(() -> {
					//System.out.println(String.format("idle timer inner: %d", idleQueue.size()));
					// TODO measure pending event backlog, adjust batch size to accomodate
					// by proxy? time since last invocation?
					for (int i = 0; i < 1000; ++i) { // Batch
						final IdleTask top = idleQueue.poll();
						if (top == null) {
							idleTimer.cancel(false);
							idleTimer = null;
							//System.out.format("Idle stopping at %d\n", i);
							break;
						} else
							top.run();
					}
					//System.out.format("Idle break at g i %d\n", GroupVisualNode.idleCount);
					idlePending = false;
				});
			}, 0, 50, TimeUnit.MILLISECONDS);
		}
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

	public class IterativeDepthFirst<T> extends IdleTask {
		private final Deque<Pair<T, Iterator<T>>> stack = new ArrayDeque<>();
		private final Function<T, Iterator<T>> descend;
		private final Consumer<T> consume;

		IterativeDepthFirst(final T root, final Function<T, Iterator<T>> descend, final Consumer<T> consume) {
			stack.add(new Pair<>(root, Iterators.forArray(root)));
			this.descend = descend;
			this.consume = consume;
		}

		public void run() {
			if (stack.isEmpty())
				return;
			final Pair<T, Iterator<T>> top = stack.getLast();
			if (top.second.hasNext()) {
				final T next = top.second.next();
				stack.addLast(new Pair<>(next, descend.apply(next)));
			} else {
				consume.accept(stack.getLast().first);
				stack.removeLast();
			}
			addIdle(this);
		}
	}

	void compact(final Context context, final Syntax.CompactionMode mode, final VisualNode root) {
		if (root.edge().converse < context.edge)
			return;
		if (idleCompact != null) {
			idleQueue.remove(idleCompact);
		}
		switch (mode) {
			case BOTTOM_UP:
				idleCompact = new IterativeDepthFirst<>(root, n -> {
					if (n.edge().converse > context.edge) {
						n.compact(context);
					}
					return n.children();
				}, n -> {
				});
				break;
			/*
			case GREATEST_GAIN:
				idleCompact = new IdleTask() {
					@Override
					boolean run() {
						if (root.converseEdge() < edge) {
							idleCompact = null;
							return false;
						}
						VisualNode at = root;
						Pair<Double, VisualNode> best = new Pair<>(at.compactionGain(edge), at);
						while (true) {
							final Optional<Pair<Double, VisualNode>> next = Helper
									.stream(at.children())
									.map(n -> new Pair<>(n.compactionGain(), n))
									.sorted((a, b) -> b.first - a.first)
									.findFirst();
							if (!next.isPresent())
								break;
							if (next.get().first > best.first)
								best = next.get();
							at = next.get().second;
						}
						best.second.compact(edge);
						return true;
					}
				};
				break;
				*/
		}
	}
}
