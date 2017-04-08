package com.zarbosoft.bonestruct.syntax.back;

import com.zarbosoft.bonestruct.document.values.ValueNode;
import com.zarbosoft.bonestruct.syntax.FreeNodeType;
import com.zarbosoft.bonestruct.syntax.InvalidSyntax;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.bonestruct.syntax.middle.MiddleNode;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.pidgoon.Node;
import com.zarbosoft.pidgoon.events.Operator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.rendaw.common.Pair;

import java.util.Set;

import static com.zarbosoft.rendaw.common.Common.iterable;

@Configuration(name = "data-node")
public class BackDataNode extends BackPart {
	@Configuration
	public String middle;

	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new Operator(new Reference(nodeType.getDataNode(middle).type), (store) -> {
			final com.zarbosoft.bonestruct.document.Node value = store.stackTop();
			store = (Store) store.popStack();
			store = (Store) store.pushStack(new Pair<>(middle, new ValueNode(nodeType.getDataNode(middle), value)));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final MiddleNode data = nodeType.getDataNode(middle);
		for (final FreeNodeType child : iterable(syntax.getLeafTypes(data.type))) {
			if (child.back.size() > 1)
				throw new InvalidSyntax(String.format(
						"Type [%s] is a child of [%s] at middle [%s], but deserializes as an array segment.",
						child.id,
						nodeType.id,
						middle
				));
		}
	}
}
