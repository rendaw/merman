package com.zarbosoft.bonestruct.modules;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Selection;
import com.zarbosoft.bonestruct.editor.banner.BannerMessage;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtom;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "selection_type")
public class SelectionType extends Module {
	@Configuration(optional = true)
	public boolean node = true;
	@Configuration(optional = true)
	public boolean part = true;

	@Override
	public State initialize(final Context context) {
		return new ModuleState(context);
	}

	private class ModuleState extends State {
		private BannerMessage message;
		private final Context.SelectionListener listener = new Context.SelectionListener() {
			@Override
			public void selectionChanged(final Context context, final Selection selection) {
				BannerMessage oldMessage = message;
				message = new BannerMessage();
				message.priority = 100;
				final StringBuilder text = new StringBuilder();
				if (node) {
					final VisualAtom nodeType = selection.getVisual().parent().atomVisual();
					if (nodeType == null)
						text.append("Root Element");
					else
						text.append(nodeType.type().name());
				}
				if (part) {
					final Visual part = selection.getVisual();
					final String temp;
					if (part instanceof VisualArray) {
						temp = "array";
					} else if (part instanceof VisualPrimitive) {
						temp = "primitive";
					} else if (part instanceof VisualNestedBase) {
						temp = "nested";
					} else
						temp = part.getClass().getSimpleName();
					if (text.length() > 0)
						text.append(" (" + temp + ")");
					else
						text.append(temp);
				}
				message.text = text.toString();
				context.banner.addMessage(context, message);
				if (oldMessage != null) {
					context.banner.removeMessage(context, oldMessage); // TODO oldMessage callback on finish?
					oldMessage = null;
				}
			}
		};

		public ModuleState(final Context context) {
			context.addSelectionListener(listener);
		}

		@Override
		public void destroy(final Context context) {
			context.removeSelectionListener(listener);
		}
	}
}
