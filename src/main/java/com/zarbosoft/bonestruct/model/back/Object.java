package com.zarbosoft.bonestruct.model.back;

import java.security.Key;
import java.util.Map;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.bonestruct.Config;
import com.zarbosoft.bonestruct.model.back.Object.ObjectData;
import com.zarbosoft.luxemj.source.LKeyEvent;
import com.zarbosoft.luxemj.source.LObjectCloseEvent;
import com.zarbosoft.luxemj.source.LObjectOpenEvent;
import com.zarbosoft.pidgoon.events.BakedOperator;
import com.zarbosoft.pidgoon.nodes.Set;

public class Object implements BackPart {
	@Config
	public Map<Key, BackPart> pairs;
	
	class ObjectData extends DataNode {
		Map<DataNode, DataNode> pairs;
		public void serialize(LuxemBuilder builder) {
			builder.objectOpen();
			pairs.forEach((k, v) -> {
				k.serialize(builder);
				v.serialize(builder);
			}); 
			builder.objectClose();
		}
	}

	public com.zarbosoft.pidgoon.internal.Node deserialize() {
		set = new Set();
		pairs.forEach((k, v) -> {
			set.add(new Sequence()
				.add(new Token(new LKeyEvent()))
				.add(v.deserialize())
			);
		});
		return new BakedOperator(
			new Sequence()
				.add(new Token(new LObjectOpenEvent()))
				.add(set)
				.add(new Token(new LObjectCloseEvent())),
			s -> {
				data = new ImmutableMap.Builder();
				IntStream.range(0, pairs.size())
					.forEach(i -> {
						value = s.popStack();
						key = s.popStack();
						data.put(key, value);
					});
				s.pushStack(new ObjectData(this, data.build()));
			}
		);
	}
}
