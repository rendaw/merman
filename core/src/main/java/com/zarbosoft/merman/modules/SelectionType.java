package com.zarbosoft.merman.modules;

import com.google.common.collect.ImmutableMap;
import com.zarbosoft.interface1.Configuration;
import com.zarbosoft.merman.document.Atom;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.Selection;
import com.zarbosoft.merman.editor.banner.BannerMessage;
import com.zarbosoft.merman.editor.visual.visuals.VisualArray;
import com.zarbosoft.merman.editor.visual.visuals.VisualAtom;
import com.zarbosoft.merman.editor.visual.visuals.VisualNestedBase;
import com.zarbosoft.merman.editor.visual.visuals.VisualPrimitive;
import com.zarbosoft.merman.syntax.format.Format;

@Configuration(name = "selection_type")
public class SelectionType extends Module {
	@Configuration
	public Format format;

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
				final String outerId;
				final String outerName;
				{
					final VisualAtom nodeType = selection.getVisual().parent().atomVisual();
					outerId = nodeType.type().id();
					outerName = nodeType.type().name();
				}
				final String part;
				final String innerId;
				final String innerName;
				{
					if (selection instanceof VisualArray.ArraySelection) {
						part = "array";
						final VisualArray.ArraySelection selection1 = (VisualArray.ArraySelection) selection;
						final Atom child = selection1.self.value.data.get(selection1.leadFirst ?
								selection1.beginIndex :
								selection1.endIndex);
						innerId = child.type.id();
						innerName = child.type.name();
					} else if (selection instanceof VisualNestedBase.NestedSelection) {
						part = "nested";
						final Atom child = ((VisualNestedBase) selection.getVisual()).atomGet();
						innerId = child.type.id();
						innerName = child.type.name();
					} else if (selection instanceof VisualPrimitive.PrimitiveSelection) {
						part = "primitive";
						innerId = outerId;
						innerName = outerName;
					} else
						throw new AssertionError();
				}
				message.text = format.format(new ImmutableMap.Builder()
						.put("outer_id", outerId)
						.put("outer_name", outerName)
						.put("part", part)
						.put("inner_id", innerId)
						.put("inner_name", innerName)
						.build());
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
