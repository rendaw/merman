package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
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
			final com.zarbosoft.bonestruct.editor.model.Node value = store.stackTop();
			store = (Store) store.popStack();
			store = (Store) store.pushStack(new Pair<>(middle,
					new DataNode.Value(nodeType.getDataNode(middle), value)
			));
			return Helper.stackSingleElement(store);
		});
	}

	public void finish(final Syntax syntax, final NodeType nodeType, final Set<String> middleUsed) {
		middleUsed.add(middle);
		final DataNode data = nodeType.getDataNode(middle);
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
