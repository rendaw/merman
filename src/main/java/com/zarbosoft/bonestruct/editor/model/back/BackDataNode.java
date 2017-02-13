package com.zarbosoft.bonestruct.editor.model.back;

import com.zarbosoft.bonestruct.Path;
import com.zarbosoft.bonestruct.editor.InvalidSyntax;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.events.Store;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;
import org.pcollections.TreePVector;

import java.util.Set;

@Luxem.Configuration(name = "data-node")
public class BackDataNode extends BackPart {
	@Luxem.Configuration
	public String middle;

	public Node buildBackRule(final Syntax syntax, final NodeType nodeType) {
		return new BakedOperator(new Reference(nodeType.getDataNode(middle).type), (store) -> {
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
		for (final FreeNodeType child : syntax.getLeafTypes(data.type)) {
			if (child.back.size() > 1)
				throw new InvalidSyntax(String.format(
						"Type [%s] is a child of [%s] at node [%s], but deserializes as an array segment.",
						child.id,
						nodeType.id,
						getPath(new Path(TreePVector.empty()))
				));
		}
	}
}
