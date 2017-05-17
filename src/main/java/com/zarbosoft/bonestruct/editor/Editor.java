package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.editor.display.Display;
import com.zarbosoft.bonestruct.editor.history.History;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.syntax.Syntax;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Editor {
	/**
	 * Invariants and inner workings
	 * - Bricks are only created
	 * -- outward from the cornerstone when the selection changes
	 * -- when the window expands
	 * -- when the model changes, at the visual level where the change occurs
	 * <p>
	 * The whole document is always loaded.
	 * Visuals exist for everything in the window.
	 * Bricks eventually exist for everything on screen.
	 * <p>
	 * The selection may be null within a transaction but always exists afterwards.
	 * The initial selection is set by default in context.
	 */
	private final Context context;
	private final Display visual;
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
		context.history.clear();
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
			keyIgnore = false;
			if (!context.keyListeners.stream().allMatch(l -> l.handleKey(context, hidEvent)))
				return;
			keyIgnore = true;
		});
		display.addTypingListener(text -> {
			if (keyIgnore) {
				keyIgnore = false;
				return;
			}
			if (text.isEmpty())
				return;
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
			context.hoverIdle.point = vector.add(new Vector(-context.syntax.padConverse, context.scroll));
		});
		visual.setBackgroundColor(context.syntax.background);
		visual.add(context.background);
		visual.add(context.midground);
		visual.add(context.foreground.visual);
		context.document.top.selectDown(context);
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
