package com.zarbosoft.bonestruct.documenter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zarbosoft.bonestruct.editor.Action;
import com.zarbosoft.bonestruct.syntax.Syntax;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.interfacedocument.Documenter;
import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.rendaw.common.ChainComparator;
import j2html.tags.ContainerTag;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.uncheck;
import static j2html.TagCreator.*;

public class Main {
	public static void main(final String[] args) {
		final Reflections reflections = new Reflections("com.zarbosoft.bonestruct");

		// Syntax file
		{
			final Map<String, String> descriptions;
			try (
					InputStream descriptionsSource = Main.class
							.getClassLoader()
							.getResourceAsStream("com/zarbosoft/bonestruct/documenter/descriptions.luxem")
			) {
				if (descriptionsSource == null)
					throw new AssertionError();
				descriptions = Luxem.<Map<String, String>>parse(
						reflections,
						new Walk.TypeInfo(Map.class, new Walk.TypeInfo(String.class), new Walk.TypeInfo(String.class)),
						descriptionsSource
				).findFirst().get();
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
			Documenter.document(
					reflections,
					descriptions,
					Paths.get("../documentation/syntax.html").toAbsolutePath(),
					Documenter.Flavor.LUA,
					"Merman",
					new Walk.TypeInfo(Syntax.class),
					ImmutableList.of("com.zarbosoft.bonestruct.", "com.zarbosoft.interface1.")
			);
		}

		// Actions
		{
			final Map<String, Map<String, String>> descriptions;
			try (
					InputStream descriptionsSource = Main.class
							.getClassLoader()
							.getResourceAsStream("com/zarbosoft/bonestruct/documenter/actionDescriptions.luxem")
			) {
				descriptions = Luxem.<Map<String, Map<String, String>>>parse(
						reflections,
						new Walk.TypeInfo(Map.class,
								new Walk.TypeInfo(String.class),
								new Walk.TypeInfo(Map.class,
										new Walk.TypeInfo(String.class),
										new Walk.TypeInfo(String.class)
								)
						),
						descriptionsSource
				).findFirst().get();
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
			final ContainerTag toc = ul();
			final ContainerTag body = body()
					.with(div().withClass("toc").with(h1("Table of Contents"), toc), a().withName("top"))
					.with(h1("Actions"));
			final Map<String, ContainerTag> tocGroups = new HashMap<>();
			final Map<String, ContainerTag> bodyGroups = new HashMap<>();
			reflections
					.getSubTypesOf(Action.class)
					.stream()
					.map(s -> (Class<?>) s)
					.filter(s -> !Modifier.isAbstract(s.getModifiers()))
					.sorted(new ChainComparator<Class<?>>()
							.lesserFirst(klass -> klass.getAnnotation(Action.StaticID.class).id())
							.build())
					.forEach(subclass -> {
						final Method groupMethod = uncheck(() -> subclass.getMethod("group"));
						groupMethod.setAccessible(true);
						final String groupName = (String) uncheck(() -> groupMethod.invoke(null));
						final String id = subclass.getAnnotation(Action.StaticID.class).id();
						final String key = String.format("#%s/%s", groupName, id);
						tocGroups
								.computeIfAbsent(groupName, k -> ul().with(h2(groupName)))
								.with(li().with(a(id).withHref(key)));
						final ContainerTag section =
								bodyGroups.computeIfAbsent(groupName, k -> div().with(h2(groupName)));
						section.with(a().withName(key), h3().with(code().withText(id)));
						{
							final String description = descriptions.getOrDefault(groupName, ImmutableMap.of()).get(id);
							if (description != null)
								section.with(p(description));
						}
					});
			tocGroups
					.entrySet()
					.stream()
					.sorted(new ChainComparator<Map.Entry<String, ContainerTag>>()
							.lesserFirst(Map.Entry::getKey)
							.build())
					.forEach(entry -> {
						toc.with(entry.getValue());
					});
			bodyGroups
					.entrySet()
					.stream()
					.sorted(new ChainComparator<Map.Entry<String, ContainerTag>>()
							.lesserFirst(Map.Entry::getKey)
							.build())
					.forEach(entry -> {
						body.with(entry.getValue());
					});
			try (
					OutputStream outStream = Files.newOutputStream(
							Paths.get("../documentation/actions.html"),
							StandardOpenOption.WRITE,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.CREATE
					)
			) {
				outStream.write(html()
						.with(head().with(title("Merman - Actions"),
								link().withRel("stylesheet").withHref("style.css")
						), body)
						.render()
						.getBytes(StandardCharsets.UTF_8));
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
