package com.zarbosoft.bonestruct.editor.model.front;

import com.zarbosoft.bonestruct.DeadCode;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.middle.DataElement;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;

import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public abstract class FrontPart {

	@Luxem.Configuration
	public Set<String> tags;

	public abstract VisualNodePart createVisual(
			Context context, Map<String, DataElement.Value> data, Set<VisualNode.Tag> tags
	);

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}

	public static abstract class DispatchHandler {

		public abstract void handle(FrontSpace front);

		public abstract void handle(FrontImage front);

		public abstract void handle(FrontMark front);

		public abstract void handle(FrontDataArray front);

		public abstract void handle(FrontDataNode front);

		public abstract void handle(FrontDataPrimitive front);

	}

	public static abstract class NodeDispatchHandler extends DispatchHandler {

		final public void handle(final FrontSpace front) {
			throw new DeadCode();
		}

		final public void handle(final FrontImage front) {
			throw new DeadCode();
		}

		final public void handle(final FrontMark front) {
			throw new DeadCode();
		}

		final public void handle(final FrontDataPrimitive front) {
			throw new DeadCode();
		}

	}

	public abstract void dispatch(DispatchHandler handler);
}
