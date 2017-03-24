package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.rendaw.common.Common;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class EditorGlobal {
	public AppDirs appDirs = new AppDirs().set_appname("androcos").set_roaming(true);
	private final SyntaxManager syntaxManager = new SyntaxManager();

	public void initializeFilesystem() {
		final Path config = appDirs.user_config_dir();
		uncheck(() -> Files.createDirectories(config));
		Collections
				.list(uncheck(() -> Thread.currentThread().getContextClassLoader().getResources("")))
				.stream()
				.filter(u -> u.getFile().endsWith(".syntax"))
				.forEach(syntax -> {
					try {
						Files.copy(syntax.openStream(), config.resolve(syntax.getFile() + ".template"));
					} catch (final FileAlreadyExistsException e) {
					} catch (final IOException e) {
						throw new Common.UncheckedException(e);
					}
				});
	}

	public Syntax getSyntax(final String id) {
		return syntaxManager.get(appDirs, id);
	}
}
