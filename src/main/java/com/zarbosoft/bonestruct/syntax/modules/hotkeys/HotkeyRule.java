package com.zarbosoft.bonestruct.syntax.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.modules.hotkeys.grammar.Node;
import com.zarbosoft.interface1.Configuration;

import java.util.*;

@Configuration
public class HotkeyRule {
	@Configuration
	public Set<Visual.Tag> tags = new HashSet<>();

	@Configuration
	public Map<String, List<Node>> hotkeys = new HashMap<>();

	@Configuration(name = "free_typing", optional = true,
			description = "Text keys that don't match a hotkey are passed to the selected primitive.")
	public boolean freeTyping = true;

	public HotkeyRule() {

	}

	public HotkeyRule(final Set<Visual.Tag> tags) {
		this.tags.addAll(tags);
	}

	public void merge(final HotkeyRule hotkeys) {
		this.hotkeys.putAll(hotkeys.hotkeys);
		freeTyping = freeTyping && hotkeys.freeTyping;
	}
}
