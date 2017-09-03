package com.zarbosoft.merman.documenter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.interface1.Walk;
import com.zarbosoft.interfacedocument.Documenter;
import com.zarbosoft.interfacedocument.FluentJSoup;
import com.zarbosoft.luxem.Luxem;
import com.zarbosoft.merman.editor.Action;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.pidgooncommand.Command;
import com.zarbosoft.rendaw.common.ChainComparator;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zarbosoft.rendaw.common.Common.iterable;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Main {
	@Configuration
	public static class CommandLine {
		@Configuration
		@Command.Argument(index = 0)
		public String path;
	}

	public static void main(final String[] args) {
		final CommandLine commandLine =
				Command.parse(new Reflections("com.zarbosoft.merman.documenter"), CommandLine.class, args);
		final Path path = Paths.get(commandLine.path).toAbsolutePath();
		final Reflections reflections = new Reflections("com.zarbosoft.merman");

		// Syntax file
		{
			final Map<String, String> descriptions;
			try (
					InputStream descriptionsSource = Main.class
							.getClassLoader()
							.getResourceAsStream("com/zarbosoft/merman/documenter/descriptions.luxem")
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
					path.resolve("syntax"),
					Documenter.Flavor.LUA,
					"Merman",
					new Walk.TypeInfo(Syntax.class),
					ImmutableList.of("com.zarbosoft.merman.", "com.zarbosoft.interface1.")
			);
		}

		// Actions
		{
			final Map<String, Map<String, String>> descriptions, extraDescriptions;
			try (
					InputStream descriptionsSource = Main.class
							.getClassLoader()
							.getResourceAsStream("com/zarbosoft/merman/documenter/actionDescriptions.luxem")
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
			extraDescriptions = new HashMap<>();
			for (final Map.Entry<String, Map<String, String>> entry : descriptions.entrySet()) {
				final Map<String, String> group = new HashMap<>();
				extraDescriptions.put(entry.getKey(), group);
				for (final Map.Entry<String, String> entry2 : entry.getValue().entrySet()) {
					group.put(entry2.getKey(), entry2.getValue());
				}
			}
			final FluentJSoup.Element toc = FluentJSoup.div();
			final FluentJSoup.Element body = FluentJSoup.div().h1("Actions");
			final Map<String, FluentJSoup.Element> tocGroups = new HashMap<>();
			final Map<String, FluentJSoup.Element> bodyGroups = new HashMap<>();
			final List<String> missingDescriptions = new ArrayList<>();
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
								.computeIfAbsent(groupName, k -> FluentJSoup.ul().h2(groupName))
								.li(li -> li.a(a -> a.text(id).attr("href", key)));
						final FluentJSoup.Element section =
								bodyGroups.computeIfAbsent(groupName, k -> FluentJSoup.div().h2(groupName));
						section.a(a -> a.attr("name", key).h3(h3 -> h3.code(id)));
						{
							final Map<String, String> descriptionGroup =
									extraDescriptions.computeIfAbsent(groupName, k -> new HashMap<>());
							String description = descriptionGroup.remove(id);
							if (description == null)
								description = descriptions.getOrDefault(groupName, ImmutableMap.of()).get(id);
							if (description == null) {
								missingDescriptions.add(String.format("%s | %s", groupName, id));
								description = "";
							}
							section.p(description);
							if (descriptionGroup.isEmpty())
								extraDescriptions.remove(groupName);
						}
					});
			if (!missingDescriptions.isEmpty() || !extraDescriptions.isEmpty()) {
				System.out.format("\n\nMISSING ACTIONS\n");
				for (final String error : iterable(missingDescriptions.stream().sorted())) {
					System.out.println(error);
				}
				System.out.format("\n\nEXTRA ACTIONS\n");
				for (final Map.Entry<String, Map<String, String>> entry : extraDescriptions.entrySet()) {
					for (final String key : entry.getValue().keySet()) {
						System.out.format("%s | %s\n", entry.getKey(), key);
					}
				}
				System.out.flush();
				System.err.flush();
				throw new AssertionError();
			}
			tocGroups
					.entrySet()
					.stream()
					.sorted(new ChainComparator<Map.Entry<String, FluentJSoup.Element>>()
							.lesserFirst(Map.Entry::getKey)
							.build())
					.forEach(entry -> {
						toc.with(entry.getValue());
					});
			bodyGroups
					.entrySet()
					.stream()
					.sorted(new ChainComparator<Map.Entry<String, FluentJSoup.Element>>()
							.lesserFirst(Map.Entry::getKey)
							.build())
					.forEach(entry -> {
						body.with(entry.getValue());
					});
			try (
					OutputStream outStream = Files.newOutputStream(
							path.resolve("Actions-Reference.md"),
							StandardOpenOption.WRITE,
							StandardOpenOption.TRUNCATE_EXISTING,
							StandardOpenOption.CREATE
					)
			) {
				outStream.write(body.render(4).getBytes(StandardCharsets.UTF_8));
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
