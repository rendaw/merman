package com.zarbosoft.bonestruct.editor.visual.visuals;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.attachments.BorderAttachment;
import com.zarbosoft.bonestruct.editor.visual.attachments.VisualAttachmentAdapter;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.rendaw.common.DeadCode;
import com.zarbosoft.rendaw.common.Pair;
import org.pcollections.PSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class VisualAtomBase extends VisualPart {
	protected VisualAtomType body;
	VisualParent parent;
	boolean selected = false;
	private VisualAttachmentAdapter adapter;
	private BorderAttachment border;
	Hoverable hoverable;
	private NestedSelection selection;
	private Brick ellipsis = null;

	public VisualAtomBase(final PSet<Tag> tags) {
		super(tags);
	}

	protected abstract void nodeSet(Context context, Atom value);

	protected abstract Atom atomGet();

	protected abstract String nodeType();

	protected abstract Value value();

	protected abstract Path getSelectionPath();

	public abstract int ellipsizeThreshold();

	protected abstract Symbol ellipsis();

	@Override
	public VisualParent parent() {
		return parent;
	}

	@Override
	public Brick getFirstBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		return body.getFirstBrick(context);
	}

	@Override
	public Brick getLastBrick(final Context context) {
		if (ellipsize(context))
			return ellipsis;
		return body.getLastBrick(context);
	}

	@Override
	public void root(
			final Context context, final VisualParent parent, final Map<String, Alignment> alignments, final int depth
	) {
		this.parent = parent;
		if (ellipsize(context)) {
			if (body != null)
				body.uproot(context, null);
		} else {
			if (body == null) {
				coreSet(context, atomGet());
			}
			body.root(context, new NestedParent(), alignments, depth);
		}
	}

	@Override
	public void uproot(final Context context, final Visual root) {
		if (selection != null)
			context.clearSelection();
		if (hoverable != null)
			context.clearHover();
		if (ellipsis != null) {
			ellipsis.destroy(context);
		}
		if (body != null)
			body.uproot(context, root);
	}

	@Override
	public Iterable<Pair<Brick, Brick.Properties>> getPropertiesForTagsChange(
			final Context context, final TagsChange change
	) {
		return body.getPropertiesForTagsChange(context, change);
	}

	private Set<Tag> ellipsisTags(final Context context) {
		return tags(context).plus(new PartTag("ellipsis"));
	}

	private Brick createEllipsis(final Context context) {
		if (ellipsis != null)
			return null;
		ellipsis = ellipsis().createBrick(context, new BrickInterface() {
			@Override
			public VisualPart getVisual() {
				return VisualAtomBase.this;
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
				return VisualAtomBase.this.getAlignment(style.alignment);
			}

			@Override
			public Set<Tag> getTags(final Context context) {
				return ellipsisTags(context);
			}
		});
		return ellipsis;
	}

	@Override
	public void tagsChanged(final Context context) {
		if (ellipsis != null) {
			final Style.Baked style = context.getStyle(ellipsisTags(context));
			ellipsis.setStyle(context, style);
		}
	}

	private boolean ellipsize(final Context context) {
		if (!context.window)
			return false;
		if (parent.getNodeVisual() == null)
			return false;
		return parent.getNodeVisual().depth >= ellipsizeThreshold();
	}

	@Override
	public Brick createFirstBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else {
			final Brick out = body.createFirstBrick(context);
			if (adapter != null) {
				adapter.setFirst(context, out);
				adapter.notifySeedBrick(context, out);
			}
			return out;
		}
	}

	@Override
	public Brick createLastBrick(final Context context) {
		if (ellipsize(context)) {
			return createEllipsis(context);
		} else {
			final Brick out = body.createLastBrick(context);
			if (adapter != null) {
				adapter.setLast(context, out);
				adapter.notifySeedBrick(context, out);
			}
			return out;
		}
	}

	private void createAdapter(final Context context) {
		adapter = new VisualAttachmentAdapter();
		adapter.setBase(context, body);
		adapter.addListener(context, new VisualAttachmentAdapter.BoundsListener() {
			@Override
			public void firstChanged(final Context context, final Brick brick) {
				border.setFirst(context, brick);
			}

			@Override
			public void lastChanged(final Context context, final Brick brick) {
				border.setLast(context, brick);
			}
		});
	}

	public void select(final Context context) {
		if (selected)
			return;
		else if (border != null) {
			context.clearHover();
		}
		final Selection selection = new NestedSelection(context);
		selected = true;
		border = new BorderAttachment(context);
		border.setStyle(context, selection.getStyle(context).obbox);
		createAdapter(context);
		context.setSelection(selection);
	}

	protected Stream<Action> getActions(final Context context) {
		return Stream.of(new Action() {
			@Override
			public void run(final Context context) {
				context.history.finishChange(context);
				body.selectDown(context);
			}

			@Override
			public String getName() {
				return "enter";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				context.history.finishChange(context);
				if (value().parent != null) {
					value().parent.selectUp(context);
				}
			}

			@Override
			public String getName() {
				return "exit";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				nodeSet(context, context.syntax.gap.create());
			}

			@Override
			public String getName() {
				return "delete";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				context.history.finishChange(context);
				context.copy(ImmutableList.of(atomGet()));
			}

			@Override
			public String getName() {
				return "copy";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				context.history.finishChange(context);
				context.copy(ImmutableList.of(atomGet()));
				nodeSet(context, context.syntax.gap.create());
			}

			@Override
			public String getName() {
				return "cut";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				context.history.finishChange(context);
				final List<Atom> atoms = context.uncopy(nodeType());
				if (atoms.size() != 1)
					return;
				nodeSet(context, atoms.get(0));
				context.history.finishChange(context);
			}

			@Override
			public String getName() {
				return "paste";
			}
		}, new Action() {
			@Override
			public void run(final Context context) {
				final Atom root = atomGet();
				context.setAtomWindow(root);
				root.visual.selectDown(context);
			}

			@Override
			public String getName() {
				return "window";
			}
		});
	}

	private class NestedSelection extends Selection {
		public NestedSelection(final Context context) {
			context.actions.put(this, VisualAtomBase.this.getActions(context).collect(Collectors.toList()));
		}

		@Override
		public void clear(final Context context) {
			border.destroy(context);
			border = null;
			adapter.destroy(context);
			adapter = null;
			selected = false;
			context.actions.remove(this);
		}

		@Override
		public VisualPart getVisual() {
			return VisualAtomBase.this;
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
		public void globalTagsChanged(final Context context) {
			border.setStyle(context, getStyle(context).obbox);
		}

		@Override
		public void addBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.addListener(context, listener);

		}

		@Override
		public void removeBrickListener(final Context context, final VisualAttachmentAdapter.BoundsListener listener) {
			adapter.removeListener(context, listener);
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
				final Visual visual = parent.getTarget();
				if (visual == this) {
					fixDeepSelection = true;
					break;
				}
				parent = visual.parent();
			}
		}
		if (hoverable == null && context.hover != null) {
			VisualParent parent = context.hover.part().parent();
			while (parent != null) {
				final Visual visual = parent.getTarget();
				if (visual == this) {
					fixDeepHover = true;
					break;
				}
				parent = visual.parent();
			}
		}

		coreSet(context, data);
		context.idleLayBricks(parent, 0, 0, 0, i -> null, i -> null, i -> body.createFirstBrick(context));

		if (fixDeepSelection)
			select(context);
		if (fixDeepHover)
			context.clearHover();
	}

	private void coreSet(final Context context, final Atom data) {
		if (body != null)
			body.uproot(context, null);
		this.body = (VisualAtomType) data.createVisual(context,
				new NestedParent(),
				parent.getTarget().alignments(),
				depth()
		);
		if (adapter != null) {
			adapter.setBase(context, body);
		}
	}

	private class NestedParent extends VisualParent {
		@Override
		public VisualParent parent() {
			return parent;
		}

		@Override
		public Visual getTarget() {
			return VisualAtomBase.this;
		}

		@Override
		public VisualAtomType getNodeVisual() {
			throw new DeadCode();
		}

		@Override
		public Alignment getAlignment(final String alignment) {
			return parent.getAlignment(alignment);
		}

		@Override
		public Brick getPreviousBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.getPreviousBrick(context);
		}

		@Override
		public Brick getNextBrick(final Context context) {
			if (parent == null)
				return null;
			return parent.getNextBrick(context);
		}

		@Override
		public Brick createNextBrick(final Context context) {
			final Brick out;
			if (parent == null)
				out = null;
			else
				out = parent.createNextBrick(context);
			if (adapter != null)
				adapter.notifyNextBrickPastEdge(context, out);
			return out;
		}

		@Override
		public Brick createPreviousBrick(final Context context) {
			final Brick out;
			if (parent == null)
				out = null;
			else
				out = parent.createPreviousBrick(context);
			if (adapter != null)
				adapter.notifyPreviousBrickPastEdge(context, out);
			return out;
		}

		@Override
		public Hoverable hover(final Context context, final Vector point) {
			if (selected)
				return null;
			{
				final Hoverable parentHoverable;
				if (parent == null)
					parentHoverable = null;
				else
					parentHoverable = parent.hover(context, point);
				if (parentHoverable != null)
					return parentHoverable;
			}
			if (hoverable != null)
				return hoverable;
			createAdapter(context);
			hoverable = new Hoverable() {
				@Override
				public void clear(final Context context) {
					border.destroy(context);
					border = null;
					adapter.destroy(context);
					adapter = null;
					hoverable = null;
				}

				@Override
				public void click(final Context context) {
					body.selectDown(context);
				}

				@Override
				public VisualAtomType node() {
					if (VisualAtomBase.this.parent == null)
						return null;
					return VisualAtomBase.this.parent.getNodeVisual();
				}

				@Override
				public VisualPart part() {
					return VisualAtomBase.this;
				}

				@Override
				public void globalTagsChanged(final Context context) {
					border.setStyle(context, getStyle(context).obbox);
				}
			};
			border = new BorderAttachment(context);
			border.setStyle(context, hoverable.getStyle(context).obbox);
			return hoverable;
		}
	}

	@Override
	public boolean selectDown(final Context context) {
		return value().selectDown(context);
	}
}
