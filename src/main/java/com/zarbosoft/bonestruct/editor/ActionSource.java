package com.zarbosoft.bonestruct.editor;

import com.zarbosoft.bonestruct.editor.visual.Visual;

import java.util.Set;
import java.util.stream.Stream;

public abstract class ActionSource {
	public abstract Stream<Action> getActions(Context context, Set<Visual.Tag> tags);
}
