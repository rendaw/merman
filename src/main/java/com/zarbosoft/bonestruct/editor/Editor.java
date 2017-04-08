package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.hid.Key;
import com.zarbosoft.bonestruct.wall.Wall;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.Parse;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Editor {
	private final Context context;
	private final Pane visual;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	private int scroll;

	public Editor(
			final EditorGlobal global,
			final Consumer<IdleTask> addIdle,
			final String path,
			final Iterable<Context.Action> globalActions,
			final History history
	) {
		final Syntax luxemSyntax = global.getSyntax("luxem");
		//final Document doc = luxemSyntax.load("[[dog, dog, dog, dog, dog, dogdogdog, dog, dog, dog]],");
		final Document doc = luxemSyntax.load("[dog, dog, dog, dog, dog, dogdogdog, dog,],");
		//final Document doc = luxemSyntax.load("[{getConverse: 47,transverse:{ar:[2,9,13]},},[atler]]");
		//final Document doc = luxemSyntax.load("[\"one\"]");
		//final Document doc = luxemSyntax.load("{analogue:bolivar}");
		final Wall wall = new Wall();
		context = new Context(luxemSyntax, doc, addIdle, wall, Iterables.concat(ImmutableList.of(new Context.Action() {
			@Override
			public void run(final Context context) {

			}

			@Override
			public String getName() {
				return "undo";
			}
		}, new Context.Action() {
			@Override
			public void run(final Context context) {

			}

			@Override
			public String getName() {
				return "redo";
			}
		}), globalActions), history);
		context.display.background = new Group();
		context.display.banner = new Context.Banner(context);
		context.plugins = doc.syntax.plugins.stream().map(p -> p.initialize(context)).collect(Collectors.toList());
		this.visual = new Pane();
		visual.setBackground(new Background(new BackgroundFill(context.syntax.background, null, null)));
		visual.getChildren().add(context.display.background);
		visual.getChildren().add(wall.visual);
		final VisualNodePart root =
				context.syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		context.selectionExtentsAdapter.addListener(context, new TransverseExtentsAdapter.Listener() {
			@Override
			public void transverseChanged(final Context context, final int transverse) {
				scrollStart = transverse;
				scrollVisible(context);
			}

			@Override
			public void transverseEdgeChanged(final Context context, final int transverse) {
				scrollEnd = transverse;
				scrollVisible(context);
			}

			@Override
			public void beddingAfterChanged(final Context context, final int beddingAfter) {
				scrollStartBeddingAfter = beddingAfter;
				scrollVisible(context);
			}

			@Override
			public void beddingBeforeChanged(final Context context, final int beddingBefore) {
				scrollStartBeddingBefore = beddingBefore;
				scrollVisible(context);
			}
		});
		root.select(context);
		visual.setOnMouseExited(event -> {
			if (context.hoverIdle != null) {
				context.hoverIdle.point = null;
			} else if (context.hover != null) {
				context.clearHover();
				context.hover = null;
				context.hoverBrick = null;
			}
		});
		visual.setOnMouseMoved(event -> {
			if (context.hoverIdle == null) {
				context.hoverIdle = context.new HoverIdle(context);
				addIdle.accept(context.hoverIdle);
			}
			context.hoverIdle.point = context
					.sceneToVector(visual, event.getX(), event.getY())
					.add(new Vector(-context.syntax.padConverse, scroll));
		});
		visual.setOnMouseClicked(event -> {
			if (context.idleClick == null) {
				context.idleClick = new IdleTask() {
					@Override
					public void runImplementation() {
						if (context.hover != null)
							context.hover.click(context);
						context.idleClick = null;
					}

					@Override
					protected void destroyed() {
						context.idleClick = null;
					}

					@Override
					protected int priority() {
						return 490;
					}
				};
				addIdle.accept(context.idleClick);
			}
		});
		final ChangeListener<Number> converseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			final int oldValue2 = (int) oldValue.doubleValue();
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
			scrollVisible(context);
		};
		switch (doc.syntax.converseDirection) {
			case UP:
			case DOWN:
				visual.heightProperty().addListener(converseSizeListener);
				visual.widthProperty().addListener(transverseSizeListener);
				break;
			case LEFT:
			case RIGHT:
				visual.widthProperty().addListener(converseSizeListener);
				visual.heightProperty().addListener(transverseSizeListener);
				break;
		}
	}

	private void scrollVisible(final Context context) {
		final int minimum = scrollStart - scrollStartBeddingBefore - context.syntax.padTransverse;
		final int maximum = scrollEnd + scrollStartBeddingAfter + context.syntax.padTransverse;
		final int maxDiff = scroll + context.transverseEdge - maximum;
		Integer newScroll = null;
		if (minimum < scroll) {
			newScroll = minimum;
		} else if (maxDiff > 0 && scroll + maxDiff < minimum) {
			newScroll = scroll + maxDiff;
		}
		if (newScroll != null) {
			context.translate(context.display.wall.visual,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			context.translate(context.display.background,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			scroll = newScroll;
			context.display.banner.setScroll(context, newScroll);
		}
	}

	public Pane getVisual() {
		return visual;
	}

	public void handleKey(final KeyEvent event) {
		if (context.hotkeyParse == null) {
			context.hotkeyParse = new Parse<Context.Action>().grammar(context.hotkeyGrammar).parse();
		}
		final Keyboard.Event keyEvent = new Keyboard.Event(Key.fromJFX(event.getCode()),
				event.isControlDown(),
				event.isAltDown(),
				event.isShiftDown()
		);
		if (context.hotkeySequence.isEmpty())
			context.hotkeySequence += keyEvent.toString();
		else
			context.hotkeySequence += ", " + keyEvent.toString();
		boolean clean = false;
		boolean receiveText = false;
		try {
			context.hotkeyParse = context.hotkeyParse.push(keyEvent, context.hotkeySequence);
		} catch (final InvalidStream e) {
			clean = true;
			receiveText = true;
		}
		final Context.Action action = context.hotkeyParse.finish();
		if (action != null) {
			clean = true;
			action.run(context);
		} else
			receiveText = true;
		if (clean) {
			context.hotkeySequence = "";
			context.hotkeyParse = null;
		}
		if (receiveText) {
			if (event.getCode() == KeyCode.ENTER)
				context.selection.receiveText(context, "\n");
			else
				context.selection.receiveText(context, event.getText());
		}
	}

	public void destroy() {
		context.plugins.forEach(p -> p.destroy(context));
		context.display.banner.destroy(context);
		context.display.wall.clear(context);
	}
}
