package com.zarbosoft.bonestruct.model;

import com.zarbosoft.bonestruct.InvalidSyntax;
import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.luxemj.LuxemEvent;
import com.zarbosoft.luxemj.path.LuxemArrayPath;
import com.zarbosoft.luxemj.path.LuxemPath;
import com.zarbosoft.pidgoon.events.EventStream;
import com.zarbosoft.pidgoon.events.Grammar;
import com.zarbosoft.pidgoon.events.Parse;
import com.zarbosoft.pidgoon.internal.Mutable;
import com.zarbosoft.pidgoon.internal.Pair;
import com.zarbosoft.pidgoon.nodes.Reference;
import com.zarbosoft.pidgoon.nodes.Union;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Luxem.Configuration
public class Syntax {

	@Luxem.Configuration
	public String name;

	@Luxem.Configuration
	public Color background;

	@Luxem.Configuration
	public List<LuxemEvent> template;

	@Luxem.Configuration
	public List<NodeType> types;

	@Luxem.Configuration
	public Map<String, java.util.Set<String>> groups;

	@Luxem.Configuration
	public String root;

	Grammar grammar;

	public static Syntax loadSyntax(final InputStream stream) {
		final Syntax out = new com.zarbosoft.luxemj.Parse<Syntax>()
				.grammar(com.zarbosoft.bonestruct.Luxem.grammarForType(Syntax.class))
				.errorHistory(5)
				.uncertainty(100)
				.node("root")
				.parse(stream);
		boolean foundRoot = false;
		final Set<String> singleNodes = new HashSet<>();
		final Set<String> arrayNodes = new HashSet<>();
		final Map<String, Pair<Boolean, Set<String>>> reverseGroups = new HashMap<>();
		for (final Map.Entry<String, Set<String>> pair : out.groups.entrySet()) {
			final String group = pair.getKey();
			final Set<String> children = pair.getValue();
			{
				final Pair<Boolean, Set<String>> set =
						reverseGroups.getOrDefault(group, new Pair<>(true, new HashSet<>()));
				reverseGroups.put(group, set);
			}
			children.forEach(child -> {
				final Pair<Boolean, Set<String>> set =
						reverseGroups.getOrDefault(child, new Pair<>(true, new HashSet<>()));
				set.second.add(group);
				reverseGroups.put(child, set);
			});
		}
		for (final NodeType t : out.types) {
			if (t.back.isEmpty())
				throw new InvalidSyntax(String.format("Type [%s] has no back parts.", t.id));
			if (arrayNodes.contains(t.id))
				throw new InvalidSyntax(String.format("Multiple types with id [%s].", t.id));
			arrayNodes.add(t.id);
			if (t.back.size() == 1)
				singleNodes.add(t.id);
			if (reverseGroups.containsKey(t.id) && t.back.size() > 1)
				reverseGroups.get(t.id).first = false;
			if (t.id.equals(out.root))
				foundRoot = true;
		}
		boolean changing = true;
		while (changing) {
			changing = false;
			for (final Map.Entry<String, Pair<Boolean, Set<String>>> pair : reverseGroups.entrySet())
				if (!pair.getValue().first)
					for (final String next : pair.getValue().second)
						if (reverseGroups.containsKey(next)) {
							final Pair<Boolean, Set<String>> reverseGroup = reverseGroups.get(next);
							if (reverseGroup.first) {
								reverseGroups.get(next).first = false;
								changing = true;
							}
						}
		}
		for (final Map.Entry<String, Set<String>> pair : out.groups.entrySet()) {
			final String group = pair.getKey();
			final Set<String> children = pair.getValue();
			if (arrayNodes.contains(group))
				throw new InvalidSyntax(String.format("Group id [%s] already used.", group));
			arrayNodes.add(group);
			if (reverseGroups.containsKey(group) && reverseGroups.get(group).first)
				singleNodes.add(group);
			if (group.equals(out.root))
				foundRoot = true;
		}
		if (!foundRoot)
			throw new InvalidSyntax(String.format("No type or tag id matches root id [%s]", out.root));
		for (final NodeType t : out.types) {
			t.finish(singleNodes, arrayNodes);
		}
		return out;
	}

	private Grammar getGrammar() {
		if (grammar == null) {
			grammar = new Grammar();
			types.forEach(t -> grammar.add(t.id, t.buildLoadRule()));
			groups.forEach((k, v) -> {
				final Union group = new Union();
				v.forEach(n -> group.add(new Reference(n)));
				grammar.add(k, group);
			});
			grammar.add("root", new Reference(root));
		}
		return grammar;
	}

	public Document create() {
		final EventStream<Node> stream = new Parse<Node>().grammar(getGrammar()).node("root").parse();
		final Mutable<LuxemPath> path = new Mutable<>(new LuxemArrayPath(null));
		template.forEach(e -> {
			path.value = path.value.push(e);
			stream.push(e, path.value.toString());
		});
		return new Document(this, stream.finish());
	}

	public Document load(final File file) throws FileNotFoundException, IOException {
		try (
				FileInputStream data = new FileInputStream(file)
		) {
			return load(data);
		}
	}

	public Document load(final String string) {
		return load(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)));
	}

	public Document load(final InputStream data) {
		return new Document(this,
				new com.zarbosoft.luxemj.Parse<Node>().grammar(getGrammar()).node("root").parse(data)
		);
	}
}
