package com.zarbosoft.bonestruct.model;

import java.util.List;
import java.util.Map;

import com.zarbosoft.bonestruct.Config;
import com.zarbosoft.bonestruct.model.back.BackPart;
import com.zarbosoft.bonestruct.model.front.FrontPart;
import com.zarbosoft.pidgoon.events.BakedOperator;

public class NodeType {
	@Config
	public String id;
	
	@Config
	public String name;
	
	@Config
	public List<FrontPart> front;
	
	@Config
	public BackPart back;

	@SuppressWarnings("unchecked")
	public com.zarbosoft.pidgoon.internal.Node buildLoadRule() {
		return new BakedOperator(
			back.buildLoadRule(),
			s -> {
				Node node = new Node();
				node.data = (Map<String, Object>) s.popStack();
				s.pushStack(node);
			});
	}
}
