package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.rendaw.common.Common;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class EditorGlobal {
	public AppDirs appDirs = new AppDirs().set_appname("androcos").set_roaming(true);
	private final SyntaxManager syntaxManager = new SyntaxManager();

	public void initializeFilesystem() {
		final Path config = appDirs.user_config_dir();
		uncheck(() -> Files.createDirectories(config));
		final Path syntaxes = config.resolve("syntaxes");
		uncheck(() -> Files.createDirectories(syntaxes));
		//new Reflections("com.zarbosoft.bonestruct.syntax", new ResourcesScanner())
		//		.getResources(Pattern.compile("\\.lua$"))
		new Reflections("com.zarbosoft.bonestruct.syntax", new ResourcesScanner())
				.getResources(Pattern.compile(".*\\.lua$"))
				.stream()
				.forEach(syntax -> {
					final Path syntaxPath = Paths.get(syntax);
					try {
						Files.copy(
								getClass().getClassLoader().getResourceAsStream(syntax),
								syntaxes.resolve(syntaxPath.getFileName())
						);
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
