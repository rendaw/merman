package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.FreeNodeType;
import com.zarbosoft.bonestruct.editor.model.Node;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class TestDocumentLoad {
	static FreeNodeType type1;
	static Syntax syntax;

	static {
		type1 = new Builders.TypeBuilder("1").back(new Builders.BackPrimitiveBuilder().value("#1").build()).build();
		syntax = new Builders.SyntaxBuilder("any")
				.type(type1)
				.group("any", new Builders.GroupBuilder().type(type1).build())
				.build();
	}

	@Test
	public void primitive() {
		final Document doc = syntax.load("#1");
		{
			final List<Node> top = doc.top.get();
			assertThat(top.size(), equalTo(1));
			assertThat(top.get(0).type, equalTo(type1));
			assertThat(top.get(0).parent.getPath(), equalTo(new Path("0")));
		}
	}

	@Test
	public void rootArray() {
		final Document doc = syntax.load("#1, #1");
		{
			final List<Node> top = doc.top.get();
			assertThat(top.size(), equalTo(2));
			assertThat(top.get(0).type, equalTo(type1));
			assertThat(top.get(1).type, equalTo(type1));
			assertThat(top.get(0), not(equalTo(top.get(1))));
			assertThat(top.get(0).parent.getPath(), equalTo(new Path("0")));
			assertThat(top.get(1).parent.getPath(), equalTo(new Path("1")));
		}
	}
}
