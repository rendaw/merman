package com.zarbosoft.bonestruct.editor.visual;

import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Luxem.Configuration
public class Hotkeys {
	@Luxem.Configuration
	public Set<VisualNode.Tag> tags = new HashSet<>();

	@Luxem.Configuration
	public Map<String, Node> hotkeys = new HashMap<>();

	public Hotkeys() {

	}

	public Hotkeys(final Set<VisualNode.Tag> tags) {
		this.tags.addAll(tags);
	}

	public void merge(final Hotkeys hotkeys) {
		this.hotkeys.putAll(hotkeys.hotkeys);
	}
}
