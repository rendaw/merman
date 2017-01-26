package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.pidgoon.internal.Helper;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class EditorGlobal {
	public AppDirs appDirs = new AppDirs().set_appname("androcos").set_roaming(true);
	private final SyntaxManager syntaxManager = new SyntaxManager();

	public EditorGlobal() {
		Luxem.grammar(); // Make sure the luxem grammar is loaded beforehand so any new resource stream doesn't get closed by that resource stream
	}

	public void initializeFilesystem() {
		final Path config = appDirs.user_config_dir();
		Helper.uncheck(() -> Files.createDirectories(config));
		Collections
				.list(Helper.uncheck(() -> Thread.currentThread().getContextClassLoader().getResources("")))
				.stream()
				.filter(u -> u.getFile().endsWith(".syntax"))
				.forEach(syntax -> {
					try {
						Files.copy(syntax.openStream(), config.resolve(syntax.getFile() + ".template"));
					} catch (final FileAlreadyExistsException e) {
					} catch (final IOException e) {
						throw new Helper.UncheckedException(e);
					}
				});
	}

	public Syntax getSyntax(final String id) {
		return syntaxManager.get(appDirs, id);
	}
}
