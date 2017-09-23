package com.zarbosoft.merman.documenter;

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
					path.resolve("syntax_reference"),
					Documenter.Flavor.LUA,
					"Merman",
					new Walk.TypeInfo(Syntax.class)
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
						final FluentJSoup.Element row = FluentJSoup.tr();
						bodyGroups.computeIfAbsent(groupName, k -> FluentJSoup.table()).with(row);
						row.with(FluentJSoup.td().a(a -> a.attr("name", key).h4(id)));
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
							final String finalDescription = description;
							row.td(td -> td.with(Documenter.transformText(finalDescription)));
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
				//throw new AssertionError();
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
						body.h2(entry.getKey());
						body.with(entry.getValue());
						body.br();
						body.br();
					});
			final Path out = path.resolve("actions_reference");
			try {
				Files.createDirectories(out);
				try (
						OutputStream outStream = Files.newOutputStream(
								out.resolve("_Sidebar.rst"),
								StandardOpenOption.WRITE,
								StandardOpenOption.TRUNCATE_EXISTING,
								StandardOpenOption.CREATE
						)
				) {
					Documenter.writeRst(outStream, toc);
				}
				try (
						OutputStream outStream = Files.newOutputStream(
								out.resolve("Actions-Reference.rst"),
								StandardOpenOption.WRITE,
								StandardOpenOption.TRUNCATE_EXISTING,
								StandardOpenOption.CREATE
						)
				) {
					Documenter.writeRst(outStream, body);
				}
			} catch (final IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
}
