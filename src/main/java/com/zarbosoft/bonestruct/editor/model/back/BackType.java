package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.luxem.read.source.LTypeEvent;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Terminal;
import com.zarbosoft.pidgoon.nodes.Sequence;

import java.util.Set;

@Configuration(name = "type")
public class BackType extends BackPart {
	@Configuration
	public String value;

	@Configuration
	public BackPart child;

	@Override
	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		super.finish(syntax, nodeType, middleUsed);
		child.finish(syntax, nodeType, middleUsed);
	}

	@Override
	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new Sequence().add(new Terminal(new LTypeEvent(value))).add(child.buildBackRule(syntax, nodeType));
	}
}
