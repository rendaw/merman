package com.zarbosoft.bonestruct;

import com.zarbosoft.bonestruct.helper.GeneralTestWizard;
import com.zarbosoft.bonestruct.helper.Helper;
import com.zarbosoft.bonestruct.helper.MiscSyntax;
import org.junit.Test;

public class TestLiveExamples {

	@Test
	public void testDeselectGapInArray() {
		new GeneralTestWizard(MiscSyntax.syntax, MiscSyntax.syntax.gap.create())
				.act("enter")
				.run(context -> context.selection.receiveText(context, "["))
				.checkBrickCount(3)
				.run(context -> context.selection.receiveText(context, "e"))
				.checkTree(new Helper.TreeBuilder(MiscSyntax.array)
						.addArray("value", new Helper.TreeBuilder(MiscSyntax.syntax.gap).add("gap", "e").build())
						.build())
				.act("exit")
				.act("exit");
	}

	@Test
	public void testDeleteArray() {
		new GeneralTestWizard(MiscSyntax.syntax, MiscSyntax.syntax.gap.create())
				.act("enter")
				.run(context -> context.selection.receiveText(context, "["))
				.act("exit")
				.act("exit")
				.checkTree(new Helper.TreeBuilder(MiscSyntax.array).addArray("value").build())
				.act("delete")
				.checkBrickCount(1);
	}
}
