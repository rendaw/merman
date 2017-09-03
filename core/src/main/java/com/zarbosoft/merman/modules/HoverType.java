package com.zarbosoft.merman.modules;

import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Hoverable;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.visual.Visual;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;

@Configuration(name = "hover_type")
public class HoverType extends Module {
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

		ModuleState(final Context context) {
			context.addHoverListener(new Context.HoverListener() {

				@Override
				public void hoverChanged(final Context context, final Hoverable hoverable) {
					BannerMessage oldMessage = message;
					message = null;
					if (hoverable != null) {
						message = new BannerMessage();
						message.priority = 100;
						final StringBuilder text = new StringBuilder();
						if (node) {
							final VisualAtom nodeType = hoverable.atom();
							if (nodeType == null)
								text.append("Root Element");
							else
								text.append((hoverable.atom()).type().name());
						}
						if (part) {
							final Visual part = hoverable.visual();
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
					}
					if (oldMessage != null) {
						context.banner.removeMessage(context, oldMessage); // TODO oldMessage callback on finish?
						oldMessage = null;
					}
				}
			});
		}

		@Override
		public void destroy(final Context context) {

		}
	}
}
