package com.zarbosoft.bonestruct.plugins;

import com.zarbosoft.bonestruct.editor.model.NodeType;
import com.zarbosoft.bonestruct.editor.model.Plugin;
import com.zarbosoft.bonestruct.editor.model.front.FrontDataRecord;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.ArrayVisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.NestedVisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.luxemj.Luxem;

@Luxem.Configuration(name = "hover-type", description = "Shows the type of the element being hovered in the banner.")
public class HoverType extends Plugin {
	@Luxem.Configuration(optional = true, description = "Include the type of the node.")
	public boolean node = true;
	@Luxem.Configuration(optional = true, description = "Include the type of the node part.")
	public boolean part = true;

	private Context.BannerMessage message;

	@Override
	public State initialize(final Context context) {
		context.addHoverListener(new Context.HoverListener() {

			@Override
			public void hoverChanged(final Context context, final Context.Hoverable hoverable) {
				if (message != null) {
					context.banner.removeMessage(context, message); // TODO message callback on finish?
					message = null;
				}
				if (hoverable == null)
					return;
				message = new Context.BannerMessage();
				message.priority = 100;
				final StringBuilder text = new StringBuilder();
				if (node)
					text.append(((NodeType.NodeTypeVisual) hoverable.node()).getType().name);
				if (part) {
					final String temp;
					final Class<?> c = hoverable.part().getClass();
					if (c == ArrayVisualNode.class) {
						temp = "array";
					} else if (c == FrontDataRecord.RecordVisual.class) {
						temp = "record";
					} else if (c == PrimitiveVisualNode.class) {
						temp = "primitive";
					} else if (c == NestedVisualNodePart.class) {
						temp = "nested";
					} else
						temp = c.getTypeName();
					if (text.length() > 0)
						text.append("(" + temp + ")");
					else
						text.append(temp);
				}
				message.text = text.toString();
				context.banner.addMessage(context, message);
			}
		});
		return new State() {
			@Override
			public void destroy(final Context context) {

			}
		};
	}
}
