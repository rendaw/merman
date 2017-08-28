package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.appdirsj.AppDirs;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.IOException;
import java.net.URL;
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
		final Path configRoot = appDirs.user_config_dir();
		uncheck(() -> Files.createDirectories(configRoot));
		final Path syntaxRoot = configRoot.resolve("syntaxes");
		uncheck(() -> Files.createDirectories(syntaxRoot));
		final Path pathPrefix = Paths.get("com/zarbosoft/bonestruct/resources/syntaxes");
		new Reflections(pathPrefix.toString().replace('/', '.'), new ResourcesScanner())
				.getResources(Pattern.compile(".*"))
				.stream()
				.forEach(file -> {
					final Path dest = syntaxRoot.resolve(pathPrefix.relativize(Paths.get(file)));
					uncheck(() -> Files.createDirectories(dest.getParent()));
					final URL sourceURL = getClass().getClassLoader().getResource(file);
					try {
						if (sourceURL.getProtocol().equals("file")) {
							// For dev use (non-jar), link rather than copy
							Files.createSymbolicLink(dest, Paths.get(sourceURL.getPath()));
						} else
							Files.copy(getClass().getClassLoader().getResourceAsStream(file), dest);
					} catch (final FileAlreadyExistsException e) {
					} catch (final IOException e) {
						e.printStackTrace();
					}
				});
	}

	public Syntax getSyntax(final String id) {
		return syntaxManager.get(appDirs, id);
	}
}
