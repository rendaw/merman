package com.zarbosoft.bonestruct.plugins;

import com.zarbosoft.bonestruct.editor.model.Plugin;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.nodes.ArrayVisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.NodeVisualNodePart;
import com.zarbosoft.bonestruct.editor.visual.nodes.PrimitiveVisualNode;
import com.zarbosoft.bonestruct.syntax.NodeType;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "hover-type", description = "Shows the type of the element being hovered in the banner.")
public class HoverType extends Plugin {
	@Configuration(optional = true, description = "Include the type of the node.")
	public boolean node = true;
	@Configuration(optional = true, description = "Include the type of the node part.")
	public boolean part = true;

	private Context.BannerMessage message;

	@Override
	public State initialize(final Context context) {
		context.addHoverListener(new Context.HoverListener() {

			@Override
			public void hoverChanged(final Context context, final Context.Hoverable hoverable) {
				Context.BannerMessage oldMessage = message;
				message = null;
				if (hoverable != null) {
					message = new Context.BannerMessage();
					message.priority = 100;
					final StringBuilder text = new StringBuilder();
					if (node) {
						final NodeType.NodeTypeVisual nodeType = hoverable.node();
						if (nodeType == null)
							text.append("Root Element");
						else
							text.append((hoverable.node()).getType().name());
					}
					if (part) {
						final String temp;
						final Class<?> c = hoverable.part().getClass();
						if (c == ArrayVisualNode.class) {
							temp = "array";
						} else if (c == PrimitiveVisualNode.class) {
							temp = "primitive";
						} else if (c == NodeVisualNodePart.class) {
							temp = "nested";
						} else
							temp = c.getTypeName();
						if (text.length() > 0)
							text.append("(" + temp + ")");
						else
							text.append(temp);
					}
					message.text = text.toString();
					context.display.banner.addMessage(context, message);
				}
				if (oldMessage != null) {
					context.display.banner.removeMessage(context, oldMessage); // TODO oldMessage callback on finish?
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
