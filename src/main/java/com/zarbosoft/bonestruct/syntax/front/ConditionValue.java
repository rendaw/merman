package com.zarbosoft.bonestruct.syntax.front;

import com.zarbosoft.bonestruct.document.Node;
import com.zarbosoft.bonestruct.document.values.Value;
import com.zarbosoft.bonestruct.document.values.ValueArray;
import com.zarbosoft.bonestruct.document.values.ValuePrimitive;
import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.visual.condition.ConditionAttachment;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.rendaw.common.DeadCode;

import java.util.List;

@Configuration(name = "value")
public class ConditionValue extends ConditionType {
	@Override
	public ConditionAttachment create(
			final Context context, final Node node
	) {
		final Value value = node.data.get(middle);
		if (value instanceof ValuePrimitive) {
			class PrimitiveCondition extends ConditionAttachment implements ValuePrimitive.Listener {

				PrimitiveCondition() {
					super(invert);
				}

				@Override
				public void destroy(final Context context) {

				}

				@Override
				public void set(final Context context, final String value) {
					if (value.isEmpty()) {
						setState(context, false);
					} else
						setState(context, true);
				}

				@Override
				public void added(final Context context, final int index, final String value) {
					setState(context, true);
				}

				@Override
				public void removed(final Context context, final int index, final int count) {
					if (((ValuePrimitive) value).get().isEmpty()) {
						setState(context, false);
					}
				}
			}
			return new PrimitiveCondition();
		} else if (value instanceof ValueArray) {
			class ArrayCondition extends ConditionAttachment implements ValueArray.Listener {

				ArrayCondition() {
					super(invert);
				}

				@Override
				public void destroy(final Context context) {

				}

				@Override
				public void changed(final Context context, final int index, final int remove, final List<Node> add) {
					if (((ValueArray) value).data.isEmpty()) {
						setState(context, false);
					} else
						setState(context, true);
				}
			}
			return new ArrayCondition();
		} else
			throw new DeadCode();
	}

	@Override
	protected boolean defaultOnImplementation() {
		if (is == ConditionValue.Is.EMPTY)
			return false;
		return true;
	}

	@Configuration
	public static enum Is {
		@Configuration(name = "empty", description = "Show if the value is empty.")
		EMPTY,
	}

	@Configuration
	public String middle;
	@Configuration
	public Is is;
}
