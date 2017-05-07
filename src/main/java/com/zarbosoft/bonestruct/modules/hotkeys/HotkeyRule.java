package com.zarbosoft.bonestruct.modules.hotkeys;

import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.modules.hotkeys.grammar.Node;
import com.zarbosoft.interface1.Configuration;

import java.util.*;

@Configuration
public class HotkeyRule {
	@Configuration(description = "These tags must be present.")
	public Set<Visual.Tag> with = new HashSet<>();
	@Configuration(optional = true, description = "These tags must be absent.")
	public Set<Visual.Tag> without = new HashSet<>();

	@Configuration(description = "Hotkeys to use when the tags match.")
	public Map<String, List<Node>> hotkeys = new HashMap<>();

	@Configuration(name = "free_typing", optional = true,
			description = "Text keys that don't match a hotkey are passed to the selected primitive.")
	public boolean freeTyping = true;

	public HotkeyRule() {

	}

	public HotkeyRule(final Set<Visual.Tag> with, final Set<Visual.Tag> without) {
		this.with.addAll(with);
		this.without.addAll(without);
	}
}
