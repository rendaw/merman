package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.rendaw.common.WeakCache;

public class SyntaxManager {
	private final WeakCache<String, Syntax> cache = new WeakCache<>(syntax -> {
		return syntax.id;
	});

	public Syntax get(final AppDirs appDirs, final String id) {
		return cache.getOrCreate(id, file -> {
			return Syntax.loadSyntax(id, appDirs.user_config_dir().resolve("syntaxes").resolve(id + ".lua"));
		});
	}
}
