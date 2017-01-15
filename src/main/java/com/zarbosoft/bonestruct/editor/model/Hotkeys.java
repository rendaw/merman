package com.zarbosoft.bonestruct.editor.model;

import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.grammar.Node;

import java.util.*;

@Luxem.Configuration
public class Hotkeys {
	@Luxem.Configuration
	public Set<VisualNode.Tag> tags = new HashSet<>();

	@Luxem.Configuration
	public Map<String, List<Node>> hotkeys = new HashMap<>();

	public Hotkeys() {

	}

	public Hotkeys(final Set<VisualNode.Tag> tags) {
		this.tags.addAll(tags);
	}

	public void merge(final Hotkeys hotkeys) {
		this.hotkeys.putAll(hotkeys.hotkeys);
	}
}
