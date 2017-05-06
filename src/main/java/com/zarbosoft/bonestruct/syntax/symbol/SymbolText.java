package com.zarbosoft.bonestruct.syntax.symbol;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.display.Text;
import com.zarbosoft.bonestruct.editor.wall.Brick;
import com.zarbosoft.bonestruct.editor.wall.BrickInterface;
import com.zarbosoft.bonestruct.editor.wall.bricks.BrickMark;
import com.zarbosoft.bonestruct.syntax.style.Style;
import com.zarbosoft.interface1.Configuration;

@Configuration(name = "text")
public class SymbolText extends Symbol {
	@Configuration
	public String text;

	public SymbolText() {

	}

	public SymbolText(final String text) {
		this.text = text;
	}

	@Override
	public DisplayNode createDisplay(final Context context) {
		final Text text = context.display.text();
		text.setText(context, this.text);
		return text;
	}

	@Override
	public void style(final Context context, final DisplayNode node, final Style.Baked style) {
		final Text text = (Text) node;
		text.setFont(context, style.getFont(context));
		text.setColor(context, style.color);
	}

	@Override
	public Brick createBrick(final Context context, final BrickInterface inter) {
		final BrickMark out = new BrickMark(context, inter);
		out.setText(context, this.text);
		return out;
	}
}
