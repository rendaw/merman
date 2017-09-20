package com.zarbosoft.merman.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.document.values.Value;
import com.zarbosoft.merman.document.values.ValueArray;
import com.zarbosoft.merman.editor.*;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.editor.visual.*;
import com.zarbosoft.merman.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.merman.editor.visual.tags.PartTag;
import com.zarbosoft.merman.editor.visual.tags.StateTag;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.editor.visual.tags.TagsChange;
import com.zarbosoft.merman.editor.wall.Brick;
import com.zarbosoft.merman.editor.wall.BrickInterface;
import com.zarbosoft.merman.syntax.style.Style;
import com.zarbosoft.merman.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class VisualNestedBase extends Visual implements VisualLeaf {
	PSet<Tag> tags;
	PSet<Tag> ellipsisTags;
	protected VisualAtom body;
	VisualParent parent;
	private NestedHoverable hoverable;
	private NestedSelection selection;
	private Brick ellipsis = null;

	public VisualNestedBase(final PSet<Tag> tags, final int visualDepth) {
		super(visualDepth);
		this.tags = tags.plus(new PartTag("atom"));
		ellipsisTags = this.tags.plus(new PartTag("ellipsis"));
	}

	protected abstract void nodeSet(Context context, Atom value);

	public abstract Atom atomGet();

	protected abstract String nodeType();

	protected abstract Value value();

	protected abstract Path getSelectionPath();

	protected abstract Symbol ellipsis();

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		if (body == null)
			return parent.getNextBrick(context);
		return body.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		if (body == null)
			return parent.getPreviousBrick(context);
		return body.getLastBrick(context);
	}

	@Override
	public void root(
			final Context context,
			final VisualParent parent,
			final Map<String, Alignment> alignments,
			final int visualDepth,
			final int depthScore
	) {
		this.parent = parent;
		if (ellipsize(context)) {
			if (body != null) {
				body.uproot(context, null);
				body = null;
				context.idleLayBricks(parent, 0, 1, 1, null, null, i -> createEllipsis(context));
			}
		} else {
			if (ellipsis != null)
				ellipsis.destroy(context);
			if (atomGet() != null) {
				if (body == null) {
					coreSet(context, atomGet());
					context.idleLayBricks(parent, 0, 1, 1, null, null, i -> body.createFirstBrick(context));
				} else
					body.root(context, new NestedParent(), alignments, visualDepth + 1, depthScore);
			}
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (selection != null)
			context.clearSelection();
		if (hoverable != null)
			context.clearHover();
		if (ellipsis != null)
			ellipsis.destroy(context);
		if (body != null) {
			body.uproot(context, root);
			body = null;
		}
	}

	private class NestedHoverable extends Hoverable {
		public final BorderAttachment border;

		private NestedHoverable(final Context context, final Brick first, final Brick last) {
			border = new BorderAttachment(context, getBorderStyle(context, tags).obbox);
			border.setFirst(context, first);
			border.setLast(context, last);
		}

		@Override
		protected void clear(final Context context) {
			border.destroy(context);
			hoverable = null;
		}

		@Override
		public void click(final Context context) {
			selectDown(context);
		}

		@Override
		public VisualAtom atom() {
			return VisualNestedBase.this.parent.atomVisual();
		}

		@Override
		public Visual visual() {
			return VisualNestedBase.this;
		}

		@Override
		public void tagsChanged(final Context context) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
		}
	}

	@Override
	public Hoverable hover(final Context context, final Vector point) {
		if (selection != null)
			return null;
		if (hoverable != null) {
		} else if (ellipsis != null) {
			hoverable = new NestedHoverable(context, ellipsis, ellipsis);
		} else {
			hoverable = new NestedHoverable(context, body.getFirstBrick(context), body.getLastBrick(context));
		}
		return hoverable;
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getLeafPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getLeafPropertiesForTagsChange(context, change);
	}

	private Brick createEllipsis(final Context context) {
		if (ellipsis != null)
			return null;
		ellipsis = ellipsis().createBrick(context, new BrickInterface() {
			@Override
			public VisualLeaf getVisual() {
				return VisualNestedBase.this;
			}

			@Override
			public Brick createPrevious(final Context context) {
				return parent.createPreviousBrick(context);
			}

			@Override
			public Brick createNext(final Context context) {
				return parent.createNextBrick(context);
			}

			@Override
			public void brickDestroyed(final Context context) {
				ellipsis = null;
			}

			@Override
			public Alignment getAlignment(final Style.Baked style) {
				return parent.atomVisual().getAlignment(style.alignment);
			}

			@Override
			public PSet<Tag> getTags(final Context context) {
				return ellipsisTags;
			}
		});
		final Style.Baked style = context.getStyle(ellipsisTags);
		ellipsis.tagsChanged(context);
		context.bricksCreated(this, ellipsis);
		return ellipsis;
	}

	public void tagsChanged(final Context context) {
		if (ellipsis != null) {
			final Style.Baked style = context.getStyle(ellipsisTags);
			ellipsis.tagsChanged(context);
		}
		if (selection != null)
			selection.tagsChanged(context);
		if (hoverable != null)
			hoverable.tagsChanged(context);
	}

	@Override
	public void changeTags(final Context context, final TagsChange change) {
		tags = change.apply(tags);
		ellipsisTags = tags.plus(new PartTag("ellipsis"));
		tagsChanged(context);
	}

	@Override
	public Stream<Brick> streamBricks() {
		if (ellipsis != null)
			return Stream.of(ellipsis);
		return body.streamBricks();
	}

	private boolean ellipsize(final Context context) {
		if (!context.window)
			return false;
		if (parent.atomVisual() == null)
			return false;
		return parent.atomVisual().depthScore >= context.syntax.ellipsizeThreshold;
	}

	@Override
	public Brick createOrGetFirstBrick(final Context context) {
		if (ellipsize(context)) {
			if (ellipsis != null)
				return ellipsis;
			return createEllipsis(context);
		} else
			return body.createOrGetFirstBrick(context);
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else {
			return body.createFirstBrick(context);
		}
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else {
			return body.createLastBrick(context);
		}
	}

	public void select(final Context context) {
		if (selection != null)
			return;
		else if (hoverable != null) {
			context.clearHover();
		}
		selection = new NestedSelection(context);
		context.setSelection(selection);
	}

	protected Stream<Action> getActions(final Context context) {
		return Stream.of(new ActionEnter(),
				new ActionExit(),
				new ActionNext(),
				new ActionPrevious(),
				new ActionDelete(),
				new ActionCopy(),
				new ActionCut(),
				new ActionPaste(),
				new ActionWindow(),
				new ActionPrefix(),
				new ActionSuffix()
		);
	}

	public class NestedSelection extends Selection {
		private BorderAttachment border;

		public NestedSelection(final Context context) {
			border = new BorderAttachment(context, getBorderStyle(context, tags).obbox);
			final Brick first = nudge(context);
			border.setFirst(context, first);
			border.setLast(context, body.getLastBrick(context));
			context.addActions(this, VisualNestedBase.this.getActions(context).collect(Collectors.toList()));
		}

		@Override
		public void clear(final Context context) {
			border.destroy(context);
			border = null;
			selection = null;
			context.removeActions(this);
		}

		@Override
		public Visual getVisual() {
			return VisualNestedBase.this;
		}

		@Override
		public SelectionState saveState() {
			return new VisualNodeSelectionState(value());
		}

		@Override
		public Path getPath() {
			return getSelectionPath();
		}

		@Override
		public void tagsChanged(
				final Context context
		) {
			border.setStyle(context, getBorderStyle(context, tags).obbox);
			super.tagsChanged(context);
		}

		@Override
		public PSet<Tag> getTags(final Context context) {
			return tags;
		}

		public Brick nudge(final Context context) {
			final Brick first = body.createOrGetFirstBrick(context);
			context.foreground.setCornerstone(context,
					first,
					() -> parent.getPreviousBrick(context),
					() -> parent.getNextBrick(context)
			);
			return first;
		}
	}

	private static class VisualNodeSelectionState implements SelectionState {
		private final Value value;

		private VisualNodeSelectionState(final Value value) {
			this.value = value;
		}

		@Override
		public void select(final Context context) {
			value.selectDown(context);
		}
	}

	protected void set(final Context context, final Atom data) {
		if (ellipsize(context))
			return;
		boolean fixDeepSelection = false;
		boolean fixDeepHover = false;
		if (context.selection != null) {
			VisualParent parent = context.selection.getVisual().parent();
			while (parent != null) {
				final Visual visual = parent.visual();
				if (visual == this) {
					fixDeepSelection = true;
					break;
				}
				parent = visual.parent();
			}
		}
		if (hoverable == null && context.hover != null) {
			VisualParent parent = context.hover.visual().parent();
			while (parent != null) {
				final Visual visual = parent.visual();
				if (visual == this) {
					fixDeepHover = true;
					break;
				}
				parent = visual.parent();
			}
		}

		coreSet(context, data);
		context.idleLayBricks(parent, 0, 1, 1, null, null, i -> body.createFirstBrick(context));

		if (fixDeepSelection)
			select(context);
		if (fixDeepHover)
			context.clearHover();
	}

	private void coreSet(final Context context, final Atom data) {
		if (body != null)
			body.uproot(context, null);
		this.body = (VisualAtom) data.createVisual(context,
				new NestedParent(),
				parent.atomVisual().alignments(),
				visualDepth + 1,
				depthScore()
		);
		if (selection != null)
			selection.nudge(context);
	}

	private class NestedParent extends VisualParent {
		@Override
		public Visual visual() {
			return VisualNestedBase.this;
		}

		@Override
		public VisualAtom atomVisual() {
			return parent.atomVisual();
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			return parent.createPreviousBrick(context);
		}

		@Override
		public Brick createNextBrick(final Context context) {
			return parent.createNextBrick(context);
		}

		@Override
		public void firstBrickChanged(final Context context, final Brick firstBrick) {
			if (selection != null)
				selection.border.setFirst(context, firstBrick);
			if (hoverable != null)
				hoverable.border.setFirst(context, firstBrick);
		}

		@Override
		public void lastBrickChanged(final Context context, final Brick lastBrick) {
			if (selection != null)
				selection.border.setFirst(context, lastBrick);
			if (hoverable != null)
				hoverable.border.setFirst(context, lastBrick);
		}

		@Override
		public Brick findPreviousBrick(final Context context) {
			return parent.findPreviousBrick(context);
		}

		@Override
		public Brick findNextBrick(final Context context) {
			return parent.findNextBrick(context);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			return parent.getPreviousBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			return parent.getNextBrick(context);
		}

		@Override
		public Hoverable hover(final Context context, final Vector point) {
			return VisualNestedBase.this.hover(context, point);
		}

		@Override
		public boolean selectNext(final Context context) {
			throw new DeadCode();
		}

		@Override
		public boolean selectPrevious(final Context context) {
			throw new DeadCode();
		}
	}

	@Override
	public boolean selectDown(final Context context) {
		return value().selectDown(context);
	}

	@Override
	public void compact(final Context context) {
		ellipsisTags = ellipsisTags.plus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
	}

	@Override
	public void expand(final Context context) {
		ellipsisTags = ellipsisTags.minus(new StateTag("compact"));
		if (ellipsis != null)
			ellipsis.tagsChanged(context);
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "atom";
		}
	}

	@Action.StaticID(id = "enter")
	private class ActionEnter extends ActionBase {
		@Override
		public boolean run(final Context context) {

			return body.selectDown(context);
		}

	}

	@Action.StaticID(id = "exit")
	private class ActionExit extends ActionBase {
		@Override
		public boolean run(final Context context) {

			if (value().parent == null)
				return false;
			return value().parent.selectUp(context);
		}

	}

	@Action.StaticID(id = "next")
	private class ActionNext extends ActionBase {
		@Override
		public boolean run(final Context context) {
			return parent.selectNext(context);
		}

	}

	@Action.StaticID(id = "previous")
	private class ActionPrevious extends ActionBase {
		@Override
		public boolean run(final Context context) {
			return parent.selectPrevious(context);
		}

	}

	@Action.StaticID(id = "delete")
	private class ActionDelete extends ActionBase {
		@Override
		public boolean run(final Context context) {
			nodeSet(context, context.syntax.gap.create());
			return true;
		}

	}

	@Action.StaticID(id = "copy")
	private class ActionCopy extends ActionBase {
		@Override
		public boolean run(final Context context) {

			context.copy(ImmutableList.of(atomGet()));
			return true;
		}

	}

	@Action.StaticID(id = "cut")
	private class ActionCut extends ActionBase {
		@Override
		public boolean run(final Context context) {

			context.copy(ImmutableList.of(atomGet()));
			nodeSet(context, context.syntax.gap.create());
			return true;
		}

	}

	@Action.StaticID(id = "paste")
	private class ActionPaste extends ActionBase {
		@Override
		public boolean run(final Context context) {

			final List<Atom> atoms = context.uncopy(nodeType());
			if (atoms.size() != 1)
				return false;
			nodeSet(context, atoms.get(0));

			return true;
		}

	}

	@Action.StaticID(id = "window")
	private class ActionWindow extends ActionBase {
		@Override
		public boolean run(final Context context) {
			final Atom root = atomGet();
			if (!root.visual.selectDown(context))
				return false;
			context.setAtomWindow(root);
			return true;
		}

	}

	@Action.StaticID(id = "prefix")
	private class ActionPrefix extends ActionBase {
		@Override
		public boolean run(final Context context) {

			final Atom old = atomGet();
			final Atom gap = context.syntax.prefixGap.create();
			nodeSet(context, gap);
			context.history.apply(context,
					new ChangeArray((ValueArray) gap.data.get("value"), 0, 0, ImmutableList.of(old))
			);
			gap.data.get("gap").selectDown(context);
			return true;
		}

	}

	@Action.StaticID(id = "suffix")
	private class ActionSuffix extends ActionBase {
		@Override
		public boolean run(final Context context) {

			final Atom gap = context.syntax.suffixGap.create(false, atomGet());
			nodeSet(context, gap);
			gap.data.get("gap").selectDown(context);
			return false;
		}
	}
}
