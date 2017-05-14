package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.SuffixGapAtomType;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "precedential")
public class ConditionNode extends ConditionType {
	@Override
	public ConditionAttachment create(
			final Context context, final Atom atom
	) {
		final boolean show;
		if (atom.parent.value().parent == null) {
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
		@Configuration(name = "precedent", description = "Show if the atom is precedent relative to its parent.")
		PRECEDENT,
	}

	@Configuration
	public Is is;
}
