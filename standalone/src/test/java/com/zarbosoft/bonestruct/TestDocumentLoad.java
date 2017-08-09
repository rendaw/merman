package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.document.Atom;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.editor.Path;
import com.zarbosoft.bonestruct.helper.GroupBuilder;
import com.zarbosoft.bonestruct.helper.SyntaxBuilder;
import com.zarbosoft.bonestruct.helper.TypeBuilder;
import com.zarbosoft.bonestruct.syntax.FreeAtomType;
import com.zarbosoft.bonestruct.syntax.Syntax;
import org.junit.Test;

import java.util.List;

import static com.zarbosoft.bonestruct.helper.Helper.buildBackPrimitive;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

public class TestDocumentLoad {
	static FreeAtomType type1;
	static Syntax syntax;

	static {
		type1 = new TypeBuilder("1").back(buildBackPrimitive("#1")).build();
		syntax = new SyntaxBuilder("any").type(type1).group("any", new GroupBuilder().type(type1).build()).build();
	}

	@Test
	public void primitive() {
		final Document doc = syntax.load("#1");
		{
			final List<Atom> top = doc.rootArray.data;
			assertThat(top.size(), equalTo(1));
			assertThat(top.get(0).type, equalTo(type1));
			assertThat(top.get(0).parent.path(), equalTo(new Path("0")));
		}
	}

	@Test
	public void rootArray() {
		final Document doc = syntax.load("#1, #1");
		{
			final List<Atom> top = doc.rootArray.data;
			assertThat(top.size(), equalTo(2));
			assertThat(top.get(0).type, equalTo(type1));
			assertThat(top.get(1).type, equalTo(type1));
			assertThat(top.get(0), not(equalTo(top.get(1))));
			assertThat(top.get(0).parent.path(), equalTo(new Path("0")));
			assertThat(top.get(1).parent.path(), equalTo(new Path("1")));
		}
	}
}
