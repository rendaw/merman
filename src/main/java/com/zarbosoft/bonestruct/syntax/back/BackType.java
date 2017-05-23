package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.syntax.AtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LTypeEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.MatchingEventTerminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.Set;

@Configuration(name = "type")
public class BackType extends BackPart {
	@Configuration
	public String value;

	@Configuration
	public BackPart child;

	@Override
	public void finish(final Syntax syntax, final AtomType atomType, final Set<String> middleUsed) {
		super.finish(syntax, atomType, middleUsed);
		child.finish(syntax, atomType, middleUsed);
		child.parent = new PartParent() {
			@Override
			public BackPart part() {
				return BackType.this;
			}

			@Override
			public String pathSection() {
				return null;
			}
		};

	}

	@Override
	public Node buildBackRule(final Syntax syntax, final AtomType atomType) {
		return new Sequence().add(new MatchingEventTerminal(new LTypeEvent(value))).add(child.buildBackRule(syntax, atomType));
	}
}
