package com.zarbosoft.merman.editor;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.display.Display;
import com.zarbosoft.merman.editor.history.History;
import com.zarbosoft.merman.syntax.Syntax;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
	 * <p>
	 * Tags are used:
	 * -- In all visuals to style bricks
	 * -- In selected visuals for tag listeners (hotkeys, indicators, etc)
	 * therefore groups and atomtype visuals (array excepted) don't need tags - only primitive/constants
	 */
	public static Supplier<Set> createSet = () -> new HashSet<>();
	private final Context context;
	private final Display visual;

	public Editor(
			final Syntax syntax,
			final Document doc,
			final Display display,
			final Consumer<IdleTask> addIdle,
			final Path path,
			final History history,
			final ClipboardEngine clipboardEngine
	) {
		context = new Context(syntax, doc, display, addIdle, history, clipboardEngine);
		context.history.clear();
		context.addActions(this, ImmutableList.of(new ActionUndo(), new ActionRedo(), new ActionClickHovered()));
		this.visual = display;
	}

	public void destroy() {
		context.modules.forEach(p -> p.destroy(context));
		context.banner.destroy(context);
		context.foreground.clear(context);
	}

	public void save(final Path dest) {
		context.document.write(dest);
		context.history.clearModified(context);
	}

	public void addActions(final Object key, final List<Action> actions) {
		context.addActions(key, actions);
	}

	public void focus() {
		context.display.focus();
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "editor";
		}
	}

	@Action.StaticID(id = "undo")
	private static class ActionUndo extends ActionBase {
		@Override
		public boolean run(final Context context) {
			return context.history.undo(context);
		}

	}

	@Action.StaticID(id = "redo")
	private static class ActionRedo extends ActionBase {
		@Override
		public boolean run(final Context context) {
			return context.history.redo(context);
		}

	}

	@Action.StaticID(id = "click_hovered")
	private static class ActionClickHovered extends ActionBase {
		@Override
		public boolean run(final Context context) {
			if (context.hover == null)
				return false;
			context.hover.click(context);
			return true;
		}
	}
}
