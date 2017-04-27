package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.SuffixGapNodeType;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "precedential")
public class ConditionNode extends ConditionType {
	@Override
	public ConditionAttachment create(
			final Context context, final Node node
	) {
		final boolean show;
		if (node.parent.value().parent == null) {
			show = true;
		} else if (!(node.type instanceof FreeNodeType)) {
			show = true;
		} else {
			show = SuffixGapNodeType.isPrecedent((FreeNodeType) node.type, node.parent, true);
		}
		final ConditionAttachment condition = new ConditionAttachment(invert) {
			@Override
			public void destroy(final Context context) {

			}
		};
		condition.setState(context, show);
		return condition;
	}

	@Configuration
	public static enum Is {
		@Configuration(name = "precedent", description = "Show if the node is precedent relative to its parent.")
		PRECEDENT,
	}

	@Configuration
	public Is is;
}
