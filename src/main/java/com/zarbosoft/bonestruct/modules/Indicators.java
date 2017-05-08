package com.zarbosoft.bonestruct.modules;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Font;
import com.zarbosoft.bonestruct.editor.display.Group;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.bonestruct.editor.visual.Visual;
import com.zarbosoft.bonestruct.syntax.symbol.Symbol;
import com.zarbosoft.interface1.Configuration;
import org.pcollections.PSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration(name = "indicators", description = "Displays a row of symbols in either gutter based on tags.")
public class Indicators extends Module {
	@Configuration
	public static class Indicator {
		@Configuration(description = "Used as type tag for styling this element.")
		public String id;

		@Configuration
		public Set<Visual.Tag> tags = new HashSet<>();

		@Configuration
		public Symbol symbol;

		DisplayNode node;
	}

	@Configuration
	public List<Indicator> indicators = new ArrayList<>();

	@Configuration(name = "converse_start", optional = true,
			description = "If true, show the symbols in the near gutter.  Otherwise, the far.")
	public boolean converseStart = true;

	@Configuration(name = "converse_padding", optional = true)
	public int conversePadding = 0;

	@Configuration(name = "transverse_start", optional = true,
			description = "If true, show the symbols at the start of the gutter.  Otherwise, the end.")
	public boolean transverseStart = true;
	@Configuration(name = "transverse_padding", optional = true)
	public int transversePadding = 0;

	private Group group;

	private final Context.ContextIntListener resizeListener = new Context.ContextIntListener() {
		@Override
		public void changed(final Context context, final int oldValue, final int newValue) {
			updatePosition(context);
		}
	};

	public void update(final Context context, final PSet<Visual.Tag> tags) {
		int transverse = 0;
		int offset = 0;
		for (final Indicator indicator : indicators) {
			if (tags.containsAll(indicator.tags)) {
				DisplayNode node = indicator.node;
				if (node == null) {
					node = indicator.node = indicator.symbol.createDisplay(context);
					group.add(offset, node);
				}
				indicator.symbol.style(
						context,
						node,
						context.getStyle(tags
								.plus(new Visual.TypeTag(indicator.id))
								.plus(new Visual.PartTag("indicator")))
				);
				final int ascent;
				final int descent;
				if (node instanceof Text) {
					final Font font = ((Text) node).font();
					ascent = font.getAscent();
					descent = font.getDescent();
				} else {
					ascent = 0;
					descent = node.transverseSpan(context);
				}
				transverse += ascent;
				node.setTransverse(context, transverse);
				transverse += descent;
				if (!converseStart) {
					node.setConverse(context, -node.converseSpan(context));
				}
				offset += 1;
			} else {
				if (indicator.node != null) {
					group.remove(offset);
					indicator.node = null;
				}
			}
		}
	}

	public void updatePosition(final Context context) {
		group.setPosition(context, new Vector(
				converseStart ? conversePadding : (context.edge - conversePadding - group.converseSpan(context)),
				transverseStart ? transversePadding : (context.edge - transversePadding)
		), false);
	}

	@Override
	public State initialize(final Context context) {
		context.addSelectionTagsChangeListener(new Context.TagsListener() {
			@Override
			public void tagsChanged(final Context context, final PSet<Visual.Tag> tags) {
				update(context, tags);
			}
		});
		context.addConverseEdgeListener(resizeListener);
		context.addTransverseEdgeListener(resizeListener);
		group = context.display.group();
		context.midground.add(group);
		update(context, context.globalTags);
		updatePosition(context);
		return new State() {
			@Override
			public void destroy(final Context context) {
				context.midground.remove(group);
				context.removeConverseEdgeListener(resizeListener);
				context.removeTransverseEdgeListener(resizeListener);
			}
		};
	}
}
