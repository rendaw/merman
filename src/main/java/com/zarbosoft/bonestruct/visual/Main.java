package com.zarbosoft.bonestruct.visual;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.model.Document;
import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.visual.nodes.parts.ArrayVisualNode;
import com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.Parse;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
	private final IdleTask idleCompact = null;

	public static void main(final String[] args) {
		launch(args);
	}

	ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	boolean idlePending = false;
	ScheduledFuture<?> idleTimer = null;
	public PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent t) {
				worker.shutdown();
			}
		});
		Luxem.grammar(); // Make sure the luxem grammar is loaded beforehand so the new resource stream doesn't get closed by that resource stream
		final Syntax luxemSyntax;
		try (
				InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("luxem.syntax")
		) {
			luxemSyntax = Syntax.loadSyntax(stream);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}
		//final Document doc = luxemSyntax.load("[[dog, dog, dog, dog, dog, dogdogdog, dog, dog, dog]],");
		final Document doc = luxemSyntax.load("[{converse: 47,transverse:{ar:[2,9,13]},},[atler]]");
		//final Document doc = luxemSyntax.load("{analogue:bolivar}");
		final Wall wall = new Wall();
		final Context context = new Context(luxemSyntax, doc, this::addIdle, wall);
		final ArrayVisualNode root =
				new ArrayVisualNode(context, doc.top, ImmutableSet.of(new VisualNode.PartTag("root"))) {

					@Override
					protected boolean tagLast() {
						return false;
					}

					@Override
					protected boolean tagFirst() {
						return false;
					}

					@Override
					protected Map<String, Node> getHotkeys() {
						return doc.syntax.rootHotkeys;
					}

					@Override
					protected List<FrontConstantPart> getPrefix() {
						return doc.syntax.rootPrefix;
					}

					@Override
					protected List<FrontConstantPart> getSeparator() {
						return doc.syntax.rootSeparator;
					}

					@Override
					protected List<FrontConstantPart> getSuffix() {
						return doc.syntax.rootSuffix;
					}
				};
		context.root(root);
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
		final Group layout = new Group();
		layout.getChildren().add(wall.visual);
		final Scene scene = new Scene(layout, 300, 275, doc.syntax.background);
			/*
		scene.setOnMouseExited(event -> {
			if (context.hoverIdle != null) {
				context.hoverIdle.point = null;
			} else if (context.hover != null) {
				context.hover.clear(context);
				context.hover = null;
			}
		});
		scene.setOnMouseMoved(event -> {
			if (context.hoverIdle == null) {
				context.hoverIdle = context.new HoverIdle(context, root);
				addIdle(context.hoverIdle);
			}
			context.hoverIdle.point = context.sceneToVector(scene,
					event.getX() - context.syntax.padHorizontal,
					event.getY() - context.syntax.padVertical
			);
		});
		*/
		scene.setOnKeyPressed(event -> {
			if (context.hotkeyParse == null) {
				context.hotkeyParse = new Parse<Context.Action>().grammar(context.hotkeyGrammar).parse();
			}
			final Keyboard.Event keyEvent = new Keyboard.Event(Keyboard.fromJFX(event.getCode()),
					event.isControlDown(),
					event.isAltDown(),
					event.isShiftDown()
			);
			if (context.hotkeySequence.isEmpty())
				context.hotkeySequence += keyEvent.toString();
			else
				context.hotkeySequence += ", " + keyEvent.toString();
			boolean clean = false;
			try {
				context.hotkeyParse = context.hotkeyParse.push(keyEvent, context.hotkeySequence);
			} catch (final InvalidStream e) {
				clean = true;
			}
			final Context.Action action = context.hotkeyParse.finish();
			if (action != null) {
				clean = true;
				action.run(context);
			}
			if (clean) {
				context.hotkeySequence = "";
				context.hotkeyParse = null;
				context.selection.receiveText(context, event.getText());
			}
		});
		final ChangeListener<Number> converseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			final int oldValue2 = (int) oldValue.doubleValue();
			//System.out.format("conv window size change: %d to %d\n", oldValue2, newValue2);
			context.edge = Math.max(0, newValue2 - doc.syntax.padConverse * 2);
			if (newValue2 < oldValue2) {
				wall.idleCompact(context);
			} else if (newValue2 > oldValue2) {
				wall.idleExpand(context);
			}
		};
		final ChangeListener<Number> transverseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			context.transverseEdge = Math.max(0, newValue2 - doc.syntax.padTransverse * 2);
		};
		switch (doc.syntax.converseDirection) {
			case UP:
				wall.visual.setLayoutY(-doc.syntax.padConverse);
				break;
			case DOWN:
				wall.visual.setLayoutY(doc.syntax.padConverse);
				break;
			case LEFT:
				wall.visual.setLayoutX(-doc.syntax.padConverse);
				break;
			case RIGHT:
				wall.visual.setLayoutX(doc.syntax.padConverse);
				break;
		}
		switch (doc.syntax.transverseDirection) {
			case UP:
				wall.visual.setLayoutY(-doc.syntax.padTransverse);
				break;
			case DOWN:
				wall.visual.setLayoutY(doc.syntax.padTransverse);
				break;
			case LEFT:
				wall.visual.setLayoutX(-doc.syntax.padTransverse);
				break;
			case RIGHT:
				wall.visual.setLayoutX(doc.syntax.padTransverse);
				break;
		}
		switch (doc.syntax.converseDirection) {
			case UP:
			case DOWN:
				/*
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				*/
				scene.heightProperty().addListener(converseSizeListener);
				//converseSizeListener.changed(null, Double.MAX_VALUE, scene.heightProperty().getValue());
				scene.widthProperty().addListener(transverseSizeListener);
				break;
			case LEFT:
			case RIGHT:
				/*
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				*/
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
							System.out.format("Idle stopping at %d\n", i);
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
}
