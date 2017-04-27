package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.banner.Banner;
import com.zarbosoft.bonestruct.editor.details.Details;
import com.zarbosoft.bonestruct.editor.hid.HIDEvent;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.modules.hotkeys.Key;
import com.zarbosoft.bonestruct.wall.Wall;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.zarbosoft.rendaw.common.Common.last;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Editor {
	private final Context context;
	private final Pane visual;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	private int scroll;
	boolean keyIgnore = false;

	public Editor(
			final EditorGlobal global, final Consumer<IdleTask> addIdle, final Path path, final History history
	) {
		final String extension = last(path.getFileName().toString().split("\\."));
		final Syntax syntax = global.getSyntax(extension);
		final Document doc;
		if (Files.exists(path))
			doc = uncheck(() -> syntax.load(path));
		else
			doc = syntax.create();
		final Wall wall = new Wall();
		context = new Context(syntax, doc, addIdle, wall, history);
		context.actions.put(this, ImmutableList.of(new Action() {
			@Override
			public void run(final Context context) {
				context.history.undo(context);
			}

			@Override
			public String getName() {
				return "undo";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				context.history.redo(context);
			}

			@Override
			public String getName() {
				return "redo";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				if (context.hover != null)
					context.hover.click(context);
			}

			@Override
			public String getName() {
				return "click_hovered";
			}
		}));
		context.display.background = new Group();
		context.display.banner = new Banner(context);
		context.display.details = new Details(context);
		context.modules = doc.syntax.modules.stream().map(p -> p.initialize(context)).collect(Collectors.toList());
		this.visual = new Pane();
		visual.setOnKeyPressed(e -> {
			final HIDEvent event = new HIDEvent(Key.fromJFX(e.getCode()), true);
			if (context.keyListeners.stream().map(l -> l.handleKey(context, event)).findFirst().orElse(false))
				return;
			keyIgnore = true;
		});
		visual.setOnKeyReleased(e -> {
			final HIDEvent event = new HIDEvent(Key.fromJFX(e.getCode()), false);
			if (context.keyListeners.stream().map(l -> l.handleKey(context, event)).findFirst().orElse(false))
				return;
			keyIgnore = true;
		});
		visual.setOnKeyTyped(e -> {
			if (keyIgnore) {
				keyIgnore = false;
				return;
			}
			if (e.getCode() == KeyCode.ENTER)
				context.selection.receiveText(context, "\n");
			else
				context.selection.receiveText(context, e.getText());
		});
		visual.setBackground(new Background(new BackgroundFill(context.syntax.background.get(), null, null)));
		visual.getChildren().add(context.display.background);
		visual.getChildren().add(wall.visual);
		final Node rootNode = new Node(ImmutableMap.of("value", doc.top));
		final VisualPart root = context.syntax.rootFront.createVisual(context, rootNode, ImmutableSet.of());
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
		root.selectDown(context);
		visual.setFocusTraversable(true);
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
		visual.setOnMousePressed(e -> {
			visual.requestFocus();
			final HIDEvent event = new HIDEvent(Key.fromJFX(e.getButton()), true);
			context.keyListeners.stream().map(l -> l.handleKey(context, event)).findFirst();
		});
		visual.setOnMouseReleased(e -> {
			visual.requestFocus();
			final HIDEvent event = new HIDEvent(Key.fromJFX(e.getButton()), false);
			context.keyListeners.stream().map(l -> l.handleKey(context, event)).findFirst();
		});
		visual.setOnScroll(e -> {
			visual.requestFocus();
			final HIDEvent event = new HIDEvent(e.getDeltaY() > 0 ? Key.MOUSE_SCROLL_DOWN : Key.MOUSE_SCROLL_UP, true);
			context.keyListeners.stream().map(l -> l.handleKey(context, event)).findFirst();
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
			context.translate(
					context.display.wall.visual,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			context.translate(
					context.display.background,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			scroll = newScroll;
			context.display.banner.setScroll(context, newScroll);
			context.display.details.setScroll(context, newScroll);
		}
	}

	public Pane getVisual() {
		return visual;
	}

	public void destroy() {
		context.modules.forEach(p -> p.destroy(context));
		context.display.banner.destroy(context);
		context.display.wall.clear(context);
	}

	public void save(final Path dest) {
		context.document.write(dest);
	}

	public void addActions(final Object object, final List<Action> actions) {
		context.actions.put(object, actions);
	}
}
