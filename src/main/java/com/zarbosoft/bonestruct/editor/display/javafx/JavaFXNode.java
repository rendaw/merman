package com.zarbosoft.bonestruct.editor.display.javafx;

import com.zarbosoft.bonestruct.editor.Context;
import com.zarbosoft.bonestruct.editor.display.DisplayNode;
import com.zarbosoft.bonestruct.editor.visual.Vector;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public abstract class JavaFXNode implements DisplayNode {
	protected abstract Node node();

	@Override
	public int converseSpan(final Context context) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return (int) node().getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node().getLayoutBounds().getWidth();
		}
		throw new DeadCode();
	}

	@Override
	public int transverseSpan(final Context context) {
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				return (int) node().getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node().getLayoutBounds().getWidth();
		}
		throw new DeadCode();
	}

	@Override
	public int converse(final Context context) {
		switch (context.syntax.converseDirection) {
			case UP:
				return context.display.edge(context) - (int) node().getLayoutY() +
						(int) node().getLayoutBounds().getWidth();
			case DOWN:
				return (int) node().getLayoutY();
			case LEFT:
				return context.display.edge(context) - (int) node().getLayoutX() +
						(int) node().getLayoutBounds().getWidth();
			case RIGHT:
				return (int) node().getLayoutX();
		}
		throw new DeadCode();
	}

	@Override
	public int transverse(final Context context) {
		switch (context.syntax.transverseDirection) {
			case UP:
				return context.display.transverseEdge(context) - (int) node().getLayoutY() +
						(int) node().getLayoutBounds().getWidth();
			case DOWN:
				return (int) node().getLayoutY();
			case LEFT:
				return context.display.transverseEdge(context) - (int) node().getLayoutX() +
						(int) node().getLayoutBounds().getWidth();
			case RIGHT:
				return (int) node().getLayoutX();
		}
		throw new DeadCode();
	}

	public Vector position(final Context context) {
		final Bounds bounds = node().getLayoutBounds();
		int converse = 0;
		int transverse = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				converse = context.display.edge(context) - (int) node().getLayoutY() + (int) bounds.getWidth();
				break;
			case DOWN:
				converse = (int) node().getLayoutY();
				break;
			case LEFT:
				converse = context.display.edge(context) - (int) node().getLayoutX() + (int) bounds.getWidth();
				break;
			case RIGHT:
				converse = (int) node().getLayoutX();
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				transverse =
						context.display.transverseEdge(context) - (int) node().getLayoutY() + (int) bounds.getWidth();
				break;
			case DOWN:
				transverse = (int) node().getLayoutY();
				break;
			case LEFT:
				transverse =
						context.display.transverseEdge(context) - (int) node().getLayoutX() + (int) bounds.getWidth();
				break;
			case RIGHT:
				transverse = (int) node().getLayoutX();
				break;
		}
		return new com.zarbosoft.bonestruct.editor.visual.Vector(converse, transverse);
	}

	private class TransitionSmoothOut extends Transition {
		private final Double diffX;
		private final Double diffY;

		{
			setCycleDuration(javafx.util.Duration.millis(200));
		}

		private TransitionSmoothOut(final Double diffX, final Double diffY) {
			this.diffX = diffX;
			this.diffY = diffY;
		}

		@Override
		protected void interpolate(final double frac) {
			final double frac2 = Math.pow(1 - frac, 3);
			if (diffX != null)
				node().setTranslateX(-frac2 * diffX);
			if (diffY != null)
				node().setTranslateY(-frac2 * diffY);
		}
	}

	@Override
	public void setTransverse(final Context context, final int transverse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (context.syntax.transverseDirection) {
			case UP:
				y = (int) node().getLayoutBounds().getHeight() + transverse;
				break;
			case DOWN:
				y = transverse;
				break;
			case LEFT:
				x = (int) node().getLayoutBounds().getWidth() + transverse;
				break;
			case RIGHT:
				x = transverse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	@Override
	public void setConverse(final Context context, final int converse, final boolean animate) {
		Integer x = null;
		Integer y = null;
		switch (context.syntax.converseDirection) {
			case UP:
				y = (int) node().getLayoutBounds().getHeight() + converse;
				break;
			case DOWN:
				y = converse;
				break;
			case LEFT:
				x = (int) node().getLayoutBounds().getWidth() + converse;
				break;
			case RIGHT:
				x = converse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	public void translate(
			final Context context, final com.zarbosoft.bonestruct.editor.visual.Vector vector, final boolean animate
	) {
		int x = 0;
		int y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = (int) node().getLayoutBounds().getHeight() + vector.converse;
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = (int) node().getLayoutBounds().getWidth() + vector.converse;
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = (int) node().getLayoutBounds().getHeight() + vector.transverse;
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = (int) node().getLayoutBounds().getWidth() + vector.transverse;
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		if (animate)
			new TransitionSmoothOut(x - node().getLayoutX(), y - node().getLayoutY()).play();
		node().setLayoutX(x);
		node().setLayoutY(y);
	}
}
