package com.zarbosoft.bonestruct.visual.nodes.parts;

import com.zarbosoft.bonestruct.Luxem;
import com.zarbosoft.bonestruct.visual.Context;
import com.zarbosoft.bonestruct.visual.alignment.Alignment;
import com.zarbosoft.bonestruct.visual.alignment.AlignmentListener;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;

public abstract class VisualNodePart extends VisualNode implements AlignmentListener {
	@Override
	public void align(final Context context) {
		parent().align(context);
	}

	@Override
	public int getConverse() {
		return startConverse();
	}

	public enum Break {
		@Luxem.Configuration(name = "always")
		ALWAYS, @Luxem.Configuration(name = "compact")
		COMPACT, @Luxem.Configuration(name = "minimal")
		MINIMAL, @Luxem.Configuration(name = "never")
		NEVER
	}

	public abstract Break breakMode();


	/*
	public abstract int spaceLeft();

	public abstract int spaceLeftCompact();

	public abstract int spaceRight();

	public abstract int spaceRightCompact();

	public abstract int spaceTop(); // Only when broken

	public abstract int spaceTopCompact(); // Only when broken

	public abstract int spaceBottom();

	public abstract int spaceBottomCompact();
	 */

	public abstract String alignmentName();

	public abstract String alignmentNameCompact();

	boolean broken = false;
	public Alignment alignment = null;
}
