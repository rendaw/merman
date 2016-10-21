package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.Vector;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.nodes.Layer;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import javafx.scene.layout.Pane;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

public abstract class GroupVisualNode extends VisualNodePart {
	static public int idleCount = 0;

	enum Compact {
		COMPACT, EXPAND
	}

	public static class ChildChange { // Only within IdleTask
		Integer converseEnd;
		Integer transverseEnd;
		Integer converseEdge;
		Integer transverseEdge;
		boolean alignment;
	}

	public Context.Hoverable hover(final Context context, final Vector point) {
		for (final VisualNodePart child : children) {
			final Context.Hoverable out = child.hover(context, point);
			if (out != null)
				return out;
		}
		return null;
	}

	@Override
	public int startConverse() {
		return converseStart;
	}

	@Override
	public int startTransverse() {
		return parentTransverseStart;
	}

	@Override
	public int startTransverseEdge() {
		return transverseStartEdge;
	}

	@Override
	public int endConverse() {
		return converseEnd;
	}

	@Override
	public int endTransverse() {
		return transverseEnd;
	}

	@Override
	public int endTransverseEdge() {
		return transverseEdge;
	}

	class IdleTask extends com.zarbosoft.bonestruct.visual.IdleTask {
		final Context context;
		Integer converseStart;
		Integer parentTransverseStart;
		Integer transverseStartEdge;
		Map<String, Alignment> alignments = new HashMap<>();

		Map<Integer, ChildChange> childChanges = new HashMap<>();
		Compact compact = null;

		IdleTask(final Context context) {
			this.context = context;
		}

		@Override
		protected int priority() {
			return -10;
		}

		@Override
		public void run() {
			idle = null;
			idleCount += 1;

			/*
			System.out.println(String.format("%s: cs %d, ts %d, ste %d, cc %d %s",
					GroupVisualNode.this,
					converseStart,
					parentTransverseStart,
					GroupVisualNode.this.transverseStartEdge,
					childChanges.size(),
					childChanges.entrySet().stream().map(e -> String.format(
							"\n\t%d (cend %d, cedge %d, tend %d, tedge %d)",
							e.getKey(),
							e.getValue().converseEnd,
							e.getValue().converseEdge,
							e.getValue().transverseEnd,
							e.getValue().transverseEdge
					)).collect(Collectors.joining(""))
			));
			*/
			{
				final VisualNode.Placement forwardAlignments = new VisualNode.Placement();
				forwardAlignments.alignments = new HashMap<>();
				for (final Map.Entry<String, Alignment> entry : alignments.entrySet()) {
					final Alignment groupAlignment = GroupVisualNode.this.alignments.get(entry.getKey());
					if (groupAlignment == null) {
						forwardAlignments.alignments.put(entry.getKey(), entry.getValue());
					} else {
						groupAlignment.place(context, entry.getValue());
					}
				}
				if (!forwardAlignments.alignments.isEmpty())
					for (final VisualNodePart child : children)
						child.place(context, forwardAlignments);
			}

			final VisualNode.Adjustment parentAdjustment = new VisualNode.Adjustment();

			if (compact == Compact.COMPACT) {
				VisualNodePart minimal = null;
				for (final VisualNodePart child : children) {
					if (child.alignmentNameCompact() != null) {
						removeAlignment(context, child);
						findAlignment(child, child.alignmentNameCompact());
					}
					child.compact(context);
					if (child.breakMode() == Break.COMPACT) {
						child.broken = true;
						placeChild(context, child);
					} else if (child.edge().converse > context.edge && minimal != null) {
						minimal.broken = true;
						placeChild(context, minimal);
					}
					if (child.broken) {
						minimal = null;
					} else {
						if (child.breakMode() == Break.MINIMAL) {
							minimal = child;
						}
					}
				}
				GroupVisualNode.this.compact = true;
			} else if (compact == Compact.EXPAND) {
				// TODO
			}

			if (converseStart != null && converseStart != GroupVisualNode.this.converseStart) {
				GroupVisualNode.this.converseStart = converseStart;
				if (children.isEmpty()) {
					GroupVisualNode.this.converseEnd = GroupVisualNode.this.converseStart;
					GroupVisualNode.this.converseEdge = GroupVisualNode.this.converseStart;
					parentAdjustment.converseEnd = converseStart;
				} else {
					placeChild(context, children.get(0));
				}
			}
			if (parentTransverseStart != null && parentTransverseStart != GroupVisualNode.this.parentTransverseStart) {
				GroupVisualNode.this.parentTransverseStart = parentTransverseStart;
				parentAdjustment.transverseEnd =
						GroupVisualNode.this.parentTransverseStart + GroupVisualNode.this.transverseEnd;
			}
			if (transverseStartEdge != null) {
				transverseStartEdge -= GroupVisualNode.this.parentTransverseStart;
				if (transverseStartEdge != GroupVisualNode.this.transverseStartEdge) {
					GroupVisualNode.this.transverseStartEdge = transverseStartEdge;
					if (children.isEmpty()) {
						transverseEdge = GroupVisualNode.this.transverseStartEdge;
						parentAdjustment.transverseEdge = GroupVisualNode.this.transverseStartEdge;
					} else
						children.stream().filter(c -> c.broken).limit(1).forEach(c -> placeChild(context, c));
				}
			}

			for (final int index : childChanges.keySet()) {
				final ChildChange change = childChanges.get(index);

				if (change.alignment) {
					placeChild(context, children.get(index));
				}

				// Change overall boundaries
				if (change.converseEdge != null) {
					if (change.converseEdge > GroupVisualNode.this.converseEdge) {
						if (primaryConverseIndex != index) {
							secondConverseEdge = GroupVisualNode.this.converseEdge;
							secondConverseIndex = primaryConverseIndex;
						}
						converseEdge = change.converseEdge;
						parentAdjustment.converseEdge = change.converseEdge;
						primaryConverseIndex = index;
					} else {
						calculateReduceConverseEdge(context, index, change.converseEdge);
					}
				}
				if (index == children.size() - 1) {
					if (change.transverseEdge != null) {
						parentAdjustment.transverseEdge =
								GroupVisualNode.this.parentTransverseStart + change.transverseEdge;
					}
					if (change.transverseEnd != null) {
						parentAdjustment.transverseEnd =
								GroupVisualNode.this.parentTransverseStart + change.transverseEnd;
					}
				}

				// Adjust next child or own bounds if last child
				/*
				System.out.println(String.format("From %s child %d of %d",
						GroupVisualNode.this,
						index,
						children.size()
				));
				*/
				if (index == children.size() - 1) {
					if (change.converseEnd != null && change.converseEnd != GroupVisualNode.this.converseEnd) {
						GroupVisualNode.this.converseEnd = change.converseEnd;
						parentAdjustment.converseEnd = GroupVisualNode.this.converseEnd;
					}
					if (change.transverseEdge != null && change.transverseEdge != GroupVisualNode.this.transverseEdge) {
						GroupVisualNode.this.transverseEdge = change.transverseEdge;
						parentAdjustment.transverseEdge =
								GroupVisualNode.this.parentTransverseStart + GroupVisualNode.this.transverseEdge;
					}
					if (change.transverseEnd != null && change.transverseEnd != GroupVisualNode.this.transverseEnd) {
						GroupVisualNode.this.transverseEnd = change.transverseEnd;
						parentAdjustment.transverseEnd =
								GroupVisualNode.this.parentTransverseStart + GroupVisualNode.this.transverseEnd;
					}
				} else {
					/*
					System.out.println(String.format("Placing %s child %d", GroupVisualNode.this, index + 1));
					*/
					placeChild(context, children.get(index + 1));
				}
			}
			childChanges.clear();

			context.translate(foreground, new Vector(0, GroupVisualNode.this.parentTransverseStart));
			context.translate(background, new Vector(0, GroupVisualNode.this.parentTransverseStart));
			if (parent != null && !parentAdjustment.isEmpty())
				parent.adjust(context, parentAdjustment);
		}
	}

	private void calculateReduceConverseEdge(final Context context, final int index, final int newConverse) {
		final VisualNode.Adjustment parentAdjustment = new VisualNode.Adjustment();
		if (index == primaryConverseIndex) {
			if (newConverse > secondConverseEdge) {
				converseEdge = newConverse;
				parentAdjustment.converseEdge = newConverse;
			} else {
				recalculateConverseEdge();
				parentAdjustment.converseEdge = GroupVisualNode.this.converseEdge;
			}
		} else if (index == secondConverseIndex) {
			if (newConverse > secondConverseEdge) {
				secondConverseEdge = newConverse;
			} else {
				recalculateConverseEdge();
			}
		}
		if (parent != null && !parentAdjustment.isEmpty())
			parent.adjust(context, parentAdjustment);
	}

	public Map<String, Alignment> alignments = new HashMap<>();
	VisualNodeParent parent = null;

	// State
	IdleTask idle;
	protected List<VisualNodePart> children = new ArrayList<>();
	Pane foreground = new Pane();
	Pane background = new Pane();
	boolean compact = false;
	int primaryConverseIndex = 0;
	int secondConverseIndex = 0;
	int secondConverseEdge = 0; // Local
	int converseStart = 0;
	int converseEnd = 0;
	int converseEdge = 0;
	int parentTransverseStart = 0;
	int transverseStartEdge = 0;
	int transverseEnd = 0;
	int transverseEdge = 0;

	@Override
	public void setParent(final VisualNodeParent parent) {
		this.parent = parent;
	}

	@Override
	public VisualNodeParent parent() {
		return parent;
	}

	@Override
	public void place(final Context context, final VisualNode.Placement placement) {
		getIdle(context);
		if (placement.converseStart != null)
			idle.converseStart = placement.converseStart;
		if (placement.parentTransverseStart != null)
			idle.parentTransverseStart = placement.parentTransverseStart;
		if (placement.transverseStartEdge != null)
			idle.transverseStartEdge = placement.transverseStartEdge;
		if (placement.alignments != null)
			idle.alignments.putAll(placement.alignments);
	}

	private void recalculateConverseEdge() {
		int newConverse = 0;
		secondConverseEdge = 0;
		for (int checkIndex = 0; checkIndex < children.size(); ++checkIndex) {
			final VisualNodePart child = children.get(checkIndex);
			if (child.edge().converse > newConverse) {
				secondConverseIndex = primaryConverseIndex;
				secondConverseEdge = GroupVisualNode.this.converseEdge;
				primaryConverseIndex = checkIndex;
				newConverse = child.edge().converse;
			}
		}
		converseEdge = newConverse;
	}

	private void placeChild(final Context context, final VisualNodePart node) {
		final VisualNode.Placement placement = new VisualNode.Placement();
		final int index = ((GroupVisualNodeParent) node.parent()).index;
		final int priorConverseEnd;
		final int priorTransverseStart;
		final int priorTransverseEdge;
		if (index == 0) {
			priorConverseEnd = converseStart;
			priorTransverseStart = 0;
			priorTransverseEdge = GroupVisualNode.this.transverseStartEdge;
		} else {
			final VisualNodePart prior = children.get(index - 1);
			priorConverseEnd = prior.end().converse;
			priorTransverseStart = prior.start().transverse;
			priorTransverseEdge = prior.edge().transverse;
		}
		placement.transverseStartEdge = priorTransverseEdge;
		if (node.broken) {
			// TODO start record of used alignments until the next break, pass on
			// TODO allow converse direction shifts at breaks, pass on until next break
			placement.parentTransverseStart = priorTransverseEdge;
		} else {
			placement.converseStart = priorConverseEnd;
			placement.parentTransverseStart = priorTransverseStart;
		}
		if (node.alignment != null)
			placement.converseStart = Math.max(placement.converseStart, node.alignment.converse);
		if (placement.converseStart != null && node.alignment != null)
			node.alignment.set(context, placement.converseStart);
		node.place(context, placement);
	}

	public void add(final Context context, final VisualNodePart node, int preindex) {
		if (preindex < 0)
			preindex = this.children.size() + preindex + 1;
		if (preindex >= this.children.size() + 1)
			throw new RuntimeException("Inserting visual node after group end.");
		final int index = preindex;

		// Adjust index refs
		this.children.stream().skip(index).forEach(n -> ((GroupVisualNodeParent) n.parent()).index += 1);
		if (idle != null) {
			idle.childChanges = idle.childChanges
					.entrySet()
					.stream()
					.collect(Collectors.toMap(e -> e.getKey() >= index ? e.getKey() + 1 : e.getKey(),
							e -> e.getValue()
					));
		}

		// Add references
		node.setParent(new GroupVisualNodeParent(this, index));
		if (!alignments.isEmpty()) {
			final VisualNode.Placement placement = new VisualNode.Placement();
			placement.alignments = Collections.unmodifiableMap(alignments);
			node.place(context, placement);
		}
		if (compact && node.alignmentNameCompact() != null)
			findAlignment(node, node.alignmentNameCompact());
		else if (node.alignmentName() != null)
			findAlignment(node, node.alignmentName());
		this.children.add(index, node);
		final Layer childVisual = node.visual();
		foreground.getChildren().add(childVisual.foreground);
		if (childVisual.background != null)
			background.getChildren().add(childVisual.background);

		// Adjust layout
		if (node.breakMode() == Break.ALWAYS || compact && node.breakMode() == Break.COMPACT)
			node.broken = true;
		placeChild(context, node);
	}

	public void add(final Context context, final VisualNodePart node) {
		add(context, node, -1);
	}

	public void remove(final Context context, int preindex) {
		if (preindex < 0)
			preindex = this.children.size() + preindex;
		if (preindex >= this.children.size() - 1)
			throw new RuntimeException("Removing visual node after group end.");
		final Integer index = preindex;
		final VisualNodePart node = children.get(index);

		// Reduce references
		removeAlignment(context, node);
		this.foreground.getChildren().remove(index);
		this.background.getChildren().remove(index);
		this.children.remove(index);

		// Adjust index references
		this.children.stream().skip(index).forEach(n -> ((GroupVisualNodeParent) n.parent()).index -= 1);
		if (idle != null) {
			idle.childChanges = idle.childChanges
					.entrySet()
					.stream()
					.filter(e -> e.getKey() != index)
					.collect(Collectors.toMap(e -> e.getKey() >= index ? index - 1 : index, e -> e.getValue()));
		}

		// Adjust layout
		calculateReduceConverseEdge(context, index, 0);
		final VisualNode.Adjustment parentAdjustment = new VisualNode.Adjustment();
		if (children.isEmpty()) {
			parentAdjustment.converseEnd = GroupVisualNode.this.converseEnd;
			parentAdjustment.transverseEnd = GroupVisualNode.this.transverseEnd;
			parentAdjustment.transverseEdge = GroupVisualNode.this.transverseStartEdge;
		} else {
			if (index == children.size()) {
				final VisualNodePart prior = children.get(index - 1);
				parentAdjustment.converseEnd = prior.end().converse;
				parentAdjustment.transverseEdge = parentTransverseStart + prior.edge().transverse;
				parentAdjustment.transverseEnd = parentTransverseStart + prior.end().transverse;
			} else {
				placeChild(context, children.get(index));
			}
		}
		parent.adjust(context, parentAdjustment);
	}

	public void remove(final Context context, final int start, final int size) {
		for (int index = start + size - 1; index >= start; --index) {
			remove(context, index);
		}
	}

	public void removeAll(final Context context) {
		remove(context, 0, children.size());
	}

	private void findAlignment(final VisualNodePart node, final String name) {
		if (name == null)
			return;
		VisualNodePart at = GroupVisualNode.this;
		while (at != null) {
			if (at instanceof GroupVisualNode && ((GroupVisualNode) at).alignments.containsKey(name)) {
				node.alignment = ((GroupVisualNode) at).alignments.get(name);
				node.alignment.listeners.add(node);
				return;
			}
			at = ((GroupVisualNode) at).parent.target();
		}
	}

	private void removeAlignment(final Context context, final VisualNodePart node) {
		if (node.alignment == null)
			return;
		node.alignment.set(context, 0);
		node.alignment.listeners.remove(node);
	}

	@Override
	public Layer visual() {
		return new Layer(foreground, background);
	}

	@Override
	public Iterator<VisualNode> children() {
		return Iterators.concat(children
				.stream()
				.map(c -> c.children())
				.toArray((IntFunction<Iterator<VisualNode>[]>) Iterator[]::new));
	}

	IdleTask getIdle(final Context context) {
		if (idle == null) {
			idle = new IdleTask(context);
			context.addIdle(idle);
		}
		return idle;
	}

	@Override
	public void compact(final Context context) {
		getIdle(context);
		idle.compact = Compact.COMPACT;
	}

	@Override
	public Vector end() {
		return new Vector(converseEnd, parentTransverseStart + transverseEnd);
	}

	@Override
	public Vector edge() {
		return new Vector(converseEdge, parentTransverseStart + transverseEdge);
	}

	@Override
	public Vector start() {
		return new Vector(converseStart, parentTransverseStart);
	}
}
