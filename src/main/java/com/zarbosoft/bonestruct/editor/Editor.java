package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zarbosoft.bonestruct.display.Display;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.attachments.TransverseExtentsAdapter;
import com.zarbosoft.bonestruct.history.History;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.pcollections.HashTreePSet;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Editor {
	private final Context context;
	private final Display visual;
	int scrollStart;
	int scrollEnd;
	int scrollStartBeddingBefore;
	int scrollStartBeddingAfter;
	private int scroll;
	boolean keyIgnore = false;

	public Editor(
			final Syntax syntax,
			final Document doc,
			final Display display,
			final Consumer<IdleTask> addIdle,
			final Path path,
			final History history
	) {
		context = new Context(syntax, doc, display, addIdle, history);
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
		context.modules = doc.syntax.modules.stream().map(p -> p.initialize(context)).collect(Collectors.toList());
		this.visual = display;
		display.addHIDEventListener(hidEvent -> {
			if (context.keyListeners.stream().map(l -> l.handleKey(context, hidEvent)).findFirst().orElse(false))
				return;
			keyIgnore = true;
		});
		display.addTypingListener(text -> {
			if (keyIgnore) {
				keyIgnore = false;
				return;
			}
			context.selection.receiveText(context, text);
		});
		display.addMouseExitListener(() -> {
			if (context.hoverIdle != null) {
				context.hoverIdle.point = null;
			} else if (context.hover != null) {
				context.clearHover();
				context.hover = null;
				context.hoverBrick = null;
			}
		});
		display.addMouseMoveListener(vector -> {
			if (context.hoverIdle == null) {
				context.hoverIdle = context.new HoverIdle(context);
				addIdle.accept(context.hoverIdle);
			}
			context.hoverIdle.point = vector.add(new Vector(-context.syntax.padConverse, scroll));
		});
		display.addTransverseEdgeListener((oldValue, newValue) -> {
			context.transverseEdge = Math.max(0, newValue - doc.syntax.padTransverse * 2);
			scrollVisible(context);
		});
		visual.setBackgroundColor(context.syntax.background);
		visual.add(context.background);
		visual.add(context.midground);
		visual.add(context.foreground.visual);
		final Node rootNode = new Node(ImmutableMap.of("value", doc.top));
		final VisualPart root = context.syntax.rootFront.createVisual(context, rootNode, HashTreePSet.empty());
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
			context.foreground.visual.setPosition(context,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			context.background.setPosition(context,
					new Vector(context.syntax.padConverse, -newScroll),
					context.syntax.animateCoursePlacement
			);
			scroll = newScroll;
			context.banner.setScroll(context, newScroll);
			context.details.setScroll(context, newScroll);
		}
	}

	public void destroy() {
		context.modules.forEach(p -> p.destroy(context));
		context.banner.destroy(context);
		context.foreground.clear(context);
	}

	public void save(final Path dest) {
		context.document.write(dest);
	}

	public void addActions(final Object object, final List<Action> actions) {
		context.actions.put(object, actions);
	}

	public void focus() {
		context.display.focus();
	}
}
