package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.DeadCode;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.back.BackPart;
import com.zarbosoft.bonestruct.editor.model.front.*;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.AlignmentDefinition;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Mutable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Luxem.Configuration
public class FreeNodeType extends NodeType {
	@Luxem.Configuration
	public String name;

	@Luxem.Configuration
	public List<FrontPart> front = new ArrayList<>();

	@Luxem.Configuration
	public List<BackPart> back = new ArrayList<>();

	@Luxem.Configuration
	public Map<String, DataElement> middle = new HashMap<>();

	@Luxem.Configuration
	public Map<String, AlignmentDefinition> alignments = new HashMap<>();

	@Luxem.Configuration(optional = true, description = "If this is an operator, the operator precedence.  This is " +
			"used when filling in a suffix gap to raise the new node to the appropriate level.  This is also used " +
			"when wrapping lines - lower precedence nodes will be compacted first, expanded last.")
	public int precedence = 0;

	@Luxem.Configuration(name = "associate-forward", optional = true, description =
			"If this is an operator, the operator associativity.  This is used when filling in a " +
					"suffix gap to raise the new node to the appropriate level.  If two operators have the same " +
					"precedence, if the higher operator is back associative the lower operator will not be raised.")
	public boolean frontAssociative = false;

	@Luxem.Configuration(name = "immediate-fill", optional = true,
			description = "When typed in a gap, immediately fill the gap with this type.")
	public boolean immediateMatch = false;

	public Node create() {
		final Map<String, DataElement.Value> data = new HashMap<>();
		middle.forEach((k, v) -> data.put(k, v.create()));
		return new Node(this, data);
	}

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, DataElement> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	protected Map<String, AlignmentDefinition> alignments() {
		return alignments;
	}

	@Override
	public int precedence() {
		return precedence;
	}

	@Override
	public boolean frontAssociative() {
		return frontAssociative;
	}

	@Override
	public String name() {
		return name;
	}

	public class GapKey {
		public int indexBefore;
		public boolean nodeBefore;
		public String key;
		public int indexAfter;
		public boolean nodeAfter;
	}

	public List<GapKey> gapKeys() {
		final List<GapKey> out = new ArrayList<>();
		final Mutable<GapKey> top = new Mutable<>(new GapKey());
		top.value.indexBefore = -1;
		final StringBuilder key = new StringBuilder();
		Helper.enumerate(front().stream()).forEach(p -> {
			p.second.dispatch(new FrontPart.DispatchHandler() {
				private void flush(final boolean skip, final boolean node) {
					// Skip = record node
					// Skip record nodes because adding a node to an empty element is ugly
					final String affix = key.toString();
					if (affix.isEmpty()) {
						if (skip) {
							top.value.indexBefore = -1;
						} else {
							top.value.indexBefore = p.first;
							top.value.nodeBefore = node;
						}
					} else {
						top.value.key = affix;
						if (skip) {
							top.value.indexAfter = -1;
						} else {
							top.value.indexAfter = p.first;
							top.value.nodeAfter = node;
						}
						out.add(top.value);
						top.value = new GapKey();
						if (skip) {
							top.value.indexBefore = -1;
						} else {
							top.value.indexBefore = p.first;
							top.value.nodeBefore = node;
						}
					}
				}

				@Override
				public void handle(final FrontSpace front) {

				}

				@Override
				public void handle(final FrontImage front) {
					key.append(front.gapKey);
				}

				@Override
				public void handle(final FrontMark front) {
					key.append(front.value);
				}

				@Override
				public void handle(final FrontDataArrayBase front) {
					front.prefix.forEach(front2 -> front2.dispatch(this));
					flush(false, true);
					front.suffix.forEach(front2 -> front2.dispatch(this));
				}

				@Override
				public void handle(final FrontDataNode front) {
					flush(false, true);
				}

				@Override
				public void handle(final FrontDataPrimitive front) {
					flush(false, false);
				}

				@Override
				public void handle(final FrontGapBase front) {
					throw new DeadCode();
				}
			});
		});
		if (key.length() > 0) {
			top.value.indexAfter = -1;
			top.value.key = key.toString();
			out.add(top.value);
		}
		return out;
	}
}
