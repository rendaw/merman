package com.zarbosoft.merman.modules.hotkeys;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.visual.tags.Tag;
import com.zarbosoft.merman.modules.hotkeys.grammar.Node;

import java.util.*;

@Configuration
public class HotkeyRule {
	@Configuration()
	public Set<Tag> with = new HashSet<>();
	@Configuration(optional = true)
	public Set<Tag> without = new HashSet<>();

	@Configuration()
	public Map<String, List<Node>> hotkeys = new HashMap<>();

	@Configuration(name = "free_typing", optional = true)
	public boolean freeTyping = true;

	public HotkeyRule() {

	}

	public HotkeyRule(final Set<Tag> with, final Set<Tag> without) {
		this.with.addAll(with);
		this.without.addAll(without);
	}
}
