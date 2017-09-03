package com.zarbosoft.merman.syntax.front;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.SuffixGapAtomType;

@Configuration(name = "precedential")
public class ConditionNode extends ConditionType {
	@Override
	public ConditionAttachment create(
			final Context context, final Atom atom
	) {
		final boolean show;
		if (atom.parent == null) {
			show = true;
		} else if (!(atom.type instanceof FreeAtomType)) {
			show = true;
		} else {
			show = SuffixGapAtomType.isPrecedent((FreeAtomType) atom.type, atom.parent, true);
		}
		final ConditionAttachment condition = new ConditionAttachment(invert) {
			@Override
			public void destroy(final Context context) {

			}
		};
		condition.setState(context, show);
		return condition;
	}

	@Override
	protected boolean defaultOnImplementation() {
		if (is == ConditionNode.Is.PRECEDENT && !invert)
			return false;
		return true;
	}

	@Configuration
	public static enum Is {
		@Configuration(name = "precedent")
		PRECEDENT,
	}

	@Configuration
	public Is is;
}
