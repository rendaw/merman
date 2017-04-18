package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.interface1.Configuration;

import java.util.Map;
import java.util.Set;

@Configuration
public abstract class FrontConstantPart extends FrontPart {

	public abstract VisualPart createVisual(Context context, Set<Visual.Tag> tags);

	@Override
	public VisualPart createVisual(
			final Context context, final Map<String, Value> data, final Set<Visual.Tag> tags
	) {
		return createVisual(context, tags);
	}

	@Override
	final public String middle() {
		return null;
	}
}
