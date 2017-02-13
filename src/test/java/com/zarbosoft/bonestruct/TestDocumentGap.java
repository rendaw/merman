package com.zarbosoft.bonestruct;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.middle.DataArrayBase;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.tree.VisualNodePart;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class TestDocumentGap {
	static FreeNodeType type1;
	static Syntax syntax;

	static {
		type1 = new Builders.TypeBuilder("1")
				.back(new Builders.BackPrimitiveBuilder().value("infinity").build())
				.front(new Builders.FrontMarkBuilder("infinity").build())
				.immediate()
				.build();
		syntax = new Builders.SyntaxBuilder("any")
				.type(type1)
				.group("any", new Builders.GroupBuilder().type(type1).build())
				.build();
	}

	@Test
	public void nullary() {
		final Document doc = syntax.create();
		final Context context = new Context(syntax, doc, null, null, null, new History());
		final Node gap = syntax.gap.create();
		context.history.apply(context, new DataArrayBase.ChangeAdd(doc.top, 0, ImmutableList.of(gap)));
		final VisualNodePart visual =
				syntax.rootFront.createVisual(context, ImmutableMap.of("value", doc.top), ImmutableSet.of());
		gap.getVisual().select(context);
		context.selection.receiveText(context, "infinity");
		{
			final List<Node> top = doc.top.get();
			assertThat(top.size(), equalTo(1));
			assertThat(top.get(0).type, equalTo(syntax.suffixGap));
			assertThat(top.get(0).parent.getPath(), equalTo(new Path("0")));
		}
	}
}
