package com.zarbosoft.bonestruct.syntax.hid;

import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.syntax.hid.grammar.Node;
import com.zarbosoft.interface1.Configuration;

import java.util.*;

@Configuration
public class Hotkeys {
	@Configuration
	public Set<VisualNode.Tag> tags = new HashSet<>();

	@Configuration
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
