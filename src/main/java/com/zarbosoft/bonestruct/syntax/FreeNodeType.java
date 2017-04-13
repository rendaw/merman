package com.zarbosoft.bonestruct.syntax;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.syntax.alignments.AlignmentDefinition;
import com.zarbosoft.bonestruct.syntax.back.BackPart;
import com.zarbosoft.bonestruct.syntax.front.FrontPart;
import com.zarbosoft.bonestruct.syntax.middle.MiddleElement;
import com.zarbosoft.interface1.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class FreeNodeType extends NodeType {
	@Configuration
	public String name;

	@Configuration
	public List<FrontPart> front = new ArrayList<>();

	@Configuration
	public List<BackPart> back = new ArrayList<>();

	@Configuration
	public Map<String, MiddleElement> middle = new HashMap<>();

	@Configuration
	public Map<String, AlignmentDefinition> alignments = new HashMap<>();

	@Configuration(optional = true, description = "If this is an operator, the operator precedence.  This is " +
			"used when filling in a suffix gap to raise the new node to the appropriate level.  This is also used " +
			"when wrapping lines - lower precedence nodes will be compacted first, expanded last.")
	public int precedence = 0;

	@Configuration(name = "associate_forward", optional = true, description =
			"If this is an operator, the operator associativity.  This is used when filling in a " +
					"suffix gap to raise the new node to the appropriate level.  If two operators have the same " +
					"precedence, if the higher operator is back associative the lower operator will not be raised.")
	public boolean frontAssociative = false;

	@Configuration(name = "auto_choose_ambiguity", optional = true,
			description = "If this type is a suggestion and there are less than this many choices, auto-choose this type.")
	public int autoChooseAmbiguity = -1;

	@Override
	public List<FrontPart> front() {
		return front;
	}

	@Override
	public Map<String, MiddleElement> middle() {
		return middle;
	}

	@Override
	public List<BackPart> back() {
		return back;
	}

	@Override
	protected Map<String, AlignmentDefinition> alignments() {
		return alignments;
	}

	@Override
	public int precedence() {
		return precedence;
	}

	@Override
	public boolean frontAssociative() {
		return frontAssociative;
	}

	@Override
	public String name() {
		return name;
	}

	public Node create(final Syntax syntax) {
		final Map<String, Value> data = new HashMap<>();
		middle.entrySet().stream().forEach(e -> data.put(e.getKey(), e.getValue().create(syntax)));
		return new Node(this, data);
	}
}
