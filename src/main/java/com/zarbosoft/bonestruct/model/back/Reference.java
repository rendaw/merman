package com.zarbosoft.bonestruct.model.back;

import com.zarbosoft.bonestruct.Config;
import com.zarbosoft.luxemj.source.LPrimitiveEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;

public class Reference implements BackPart {
	@Config
	public String name;
	
	public com.zarbosoft.pidgoon.internal.Node deserialize() {
		return new BakedOperator(
			new Token(new LPrimitiveEvent(type)),
			s -> {
				s.pushStack(new Pair<>(name, (String)s.popData()));
			}
		);
	}
}
