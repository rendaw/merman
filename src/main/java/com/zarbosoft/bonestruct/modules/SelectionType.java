package com.zarbosoft.bonestruct.modules;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.Selection;
import com.zarbosoft.bonestruct.editor.banner.BannerMessage;
import com.zarbosoft.bonestruct.editor.visual.VisualPart;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualArray;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtomBase;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualAtomType;
import com.zarbosoft.bonestruct.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "selection_type", description = "Shows the type of the selected element in the banner.")
public class SelectionType extends Module {
	@Configuration(optional = true, description = "Include the type of the atom.")
	public boolean node = true;
	@Configuration(optional = true, description = "Include the type of the atom part.")
	public boolean part = true;

	private BannerMessage message;

	@Override
	public State initialize(final Context context) {
		context.addSelectionListener(new Context.SelectionListener() {
			@Override
			public void selectionChanged(final Context context, final Selection selection) {
				BannerMessage oldMessage = message;
				message = new BannerMessage();
				message.priority = 100;
				final StringBuilder text = new StringBuilder();
				if (node) {
					final VisualAtomType nodeType = selection.getVisual().parent().getNodeVisual();
					if (nodeType == null)
						text.append("Root Element");
					else
						text.append(nodeType.getType().name());
				}
				if (part) {
					final VisualPart part = selection.getVisual();
					final String temp;
					if (part instanceof VisualArray) {
						temp = "array";
					} else if (part instanceof VisualPrimitive) {
						temp = "primitive";
					} else if (part instanceof VisualAtomBase) {
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
		});
		return new State() {
			@Override
			public void destroy(final Context context) {

			}
		};
	}
}
