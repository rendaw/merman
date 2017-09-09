package com.zarbosoft.merman;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.history.changes.ChangeArray;
import com.zarbosoft.merman.helper.*;
import com.zarbosoft.merman.syntax.FreeAtomType;
import com.zarbosoft.merman.syntax.Syntax;
import org.junit.Test;

import java.util.function.Consumer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestHistory {
	final public static FreeAtomType one;
	final public static Syntax syntax;
	final public static Consumer<Context> modify;
	final public static Consumer<Context> undo;
	final public static Consumer<Context> redo;

	static {
		one = new TypeBuilder("one")
				.back(Helper.buildBackPrimitive("one"))
				.front(new FrontMarkBuilder("one").build())
				.build();
		syntax = new SyntaxBuilder("any").type(one).group("any", new GroupBuilder().type(one).build()).build();
		modify = new Consumer<>() {
			@Override
			public void accept(final Context context) {
				context.history.apply(context, new ChangeArray(Helper.rootArray(context.document),
						0,
						0,
						ImmutableList.of(new TreeBuilder(one).build())
				));
				context.history.finishChange(context);
			}
		};
		undo = new Consumer<>() {
			@Override
			public void accept(final Context context) {
				context.history.undo(context);
			}
		};
		redo = new Consumer<>() {
			@Override
			public void accept(final Context context) {
				context.history.redo(context);
			}
		};
	}

	public GeneralTestWizard initializeWithALongNameToForceChainWrapping() {
		return new GeneralTestWizard(syntax).run(context -> context.history.clear());
	}

	@Test
	public void testEmptyClear() {
		initializeWithALongNameToForceChainWrapping().run(context -> assertThat(context.history.isModified(),
				is(false)
		));
	}

	@Test
	public void testChange() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> assertThat(context.history.isModified(), is(true)));
	}

	@Test
	public void testUndoClear() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(undo)
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testRedoChange() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(undo)
				.run(redo)
				.run(context -> assertThat(context.history.isModified(), is(true)));
	}

	@Test
	public void testChangeClear() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testClearUndoChanged() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(undo)
				.run(context -> assertThat(context.history.isModified(), is(true)));
	}

	@Test
	public void testClearRedoClear() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(undo)
				.run(redo)
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testLateChangeClear() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(modify)
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testLateClearUndoChanged() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(modify)
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(undo)
				.run(context -> assertThat(context.history.isModified(), is(true)));
	}

	@Test
	public void testLateClearRedoClear() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(modify)
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(undo)
				.run(redo)
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testChangeFinishChange() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.finishChange(context))
				.run(modify)
				.run(undo)
				.checkArrayTree(new TreeBuilder(one).build(), new TreeBuilder(one).build());
	}

	@Test
	public void testClearModifiedChangeUndo() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.clearModified(context))
				.run(modify)
				.run(undo)
				.run(context -> assertThat(context.history.isModified(), is(false)));
	}

	@Test
	public void testModifyAfterFinishUndo() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.finishChange(context))
				.run(modify)
				.run(undo)
				.run(modify)
				.run(undo)
				.checkArrayTree(new TreeBuilder(one).build(), new TreeBuilder(one).build());
	}

	@Test
	public void testModifyAfterFinishRedo() {
		initializeWithALongNameToForceChainWrapping()
				.run(modify)
				.run(context -> context.history.finishChange(context))
				.run(undo)
				.run(redo)
				.run(modify)
				.run(undo)
				.checkArrayTree(new TreeBuilder(one).build(), new TreeBuilder(one).build());
	}
}
