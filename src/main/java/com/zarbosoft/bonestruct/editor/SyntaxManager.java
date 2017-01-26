package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.WeakCache;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.pidgoon.internal.Helper;

import java.io.InputStream;
import java.nio.file.Files;

public class SyntaxManager {
	private final WeakCache<String, Syntax> cache = new WeakCache<>(syntax -> {
		return syntax.id;
	});

	public Syntax get(final AppDirs appDirs, final String id) {
		return cache.getOrCreate(id, file -> {
			InputStream inputStream;
			try {
				inputStream =
						Helper.uncheck(() -> Files.newInputStream(appDirs.user_config_dir().resolve(id + ".syntax")));
			} catch (final Helper.UncheckedNoSuchFileException e) {
				inputStream = Helper.uncheck(() -> Thread
						.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(id + ".syntax"));
			}
			return Syntax.loadSyntax(id, inputStream);
		});
	}
}
