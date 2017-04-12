package com.zarbosoft.bonestruct.syntax.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.visual.tree.VisualNode;
import com.zarbosoft.bonestruct.syntax.hid.grammar.Node;
import com.zarbosoft.interface1.Configuration;

import java.util.*;

@Configuration
public class HotkeyRule {
	@Configuration
	public Set<VisualNode.Tag> tags = new HashSet<>();

	@Configuration
	public Map<String, List<Node>> hotkeys = new HashMap<>();

	public HotkeyRule() {

	}

	public HotkeyRule(final Set<VisualNode.Tag> tags) {
		this.tags.addAll(tags);
	}

	public void merge(final HotkeyRule hotkeys) {
		this.hotkeys.putAll(hotkeys.hotkeys);
	}
}
