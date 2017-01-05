package com.zarbosoft.bonestruct.tools;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.luxem.Documenter;
import com.zarbosoft.bonestruct.editor.model.Syntax;

import java.io.File;

public class Documentation {
	public static void main(final String[] args) {
		final File file = new File("documentation/syntax-documentation.html");
		Documenter.get().document(
				file,
				Syntax.class,
				ImmutableList.of("com.zarbosoft.bonestruct.editor.", "com.zarbosoft.luxemj.", "model.")
		);
		System.out.format("Wrote documentation to [%s]\n", file);
	}
}
