package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public abstract class FrontPart {

	@Configuration
	public Set<String> tags = new HashSet<>();

	public abstract VisualPart createVisual(
			Context context, Map<String, Value> data, Set<Visual.Tag> tags
	);

	public void finish(final NodeType nodeType, final Set<String> middleUsed) {
	}

	public abstract String middle();

	public static abstract class DispatchHandler {

		public abstract void handle(FrontSpace front);

		public abstract void handle(FrontImage front);

		public abstract void handle(FrontMark front);

		public abstract void handle(FrontDataArrayBase front);

		public abstract void handle(FrontDataNode front);

		public abstract void handle(FrontDataPrimitive front);

		public abstract void handle(FrontGapBase front);
	}

	public static abstract class NodeDispatchHandler extends DispatchHandler {

		final public void handle(final FrontSpace front) {
		}

		final public void handle(final FrontImage front) {
		}

		final public void handle(final FrontMark front) {
		}

		final public void handle(final FrontDataPrimitive front) {
		}

		@Override
		final public void handle(final FrontGapBase front) {
		}
	}

	public static abstract class NodeOnlyDispatchHandler extends DispatchHandler {

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

		@Override
		final public void handle(final FrontGapBase front) {
			throw new DeadCode();
		}
	}

	public abstract void dispatch(DispatchHandler handler);

	public abstract static class DataDispatchHandler extends DispatchHandler {
		@Override
		public void handle(final FrontSpace front) {

		}

		@Override
		public void handle(final FrontImage front) {

		}

		@Override
		public void handle(final FrontMark front) {

		}
	}
}
