package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.rendaw.common.Common;
import com.zarbosoft.rendaw.common.WeakCache;

import java.io.InputStream;
import java.nio.file.Files;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class SyntaxManager {
	private final WeakCache<String, Syntax> cache = new WeakCache<>(syntax -> {
		return syntax.id;
	});

	public Syntax get(final AppDirs appDirs, final String id) {
		return cache.getOrCreate(id, file -> {
			InputStream inputStream;
			try {
				inputStream = uncheck(() -> Files.newInputStream(appDirs.user_config_dir().resolve(id + ".syntax")));
			} catch (final Common.UncheckedNoSuchFileException e) {
				inputStream = uncheck(() -> Thread
						.currentThread()
						.getContextClassLoader()
						.getResourceAsStream(id + ".syntax"));
			}
			return Syntax.loadSyntax(id, inputStream);
		});
	}
}
