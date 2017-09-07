package com.zarbosoft.merman.editor.display.javafx;

import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.DisplayNode;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.scene.Node;

public abstract class JavaFXNode implements DisplayNode {
	protected abstract Node node();

	protected class Properties {
		final int width;
		final int height;

		Properties() {
			final Bounds bounds = node().getLayoutBounds();
			width = (int) bounds.getWidth();
			height = (int) bounds.getHeight();
		}
	}

	protected Properties properties = null;

	protected void properties() {
		if (properties == null)
			properties = new Properties();
	}

	@Override
	public int converseSpan(final Context context) {
		properties();
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return (int) properties.height;
			case LEFT:
			case RIGHT:
				return (int) properties.width;
		}
		throw new DeadCode();
	}

	@Override
	public int transverseSpan(final Context context) {
		properties();
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				return (int) properties.height;
			case LEFT:
			case RIGHT:
				return (int) properties.width;
		}
		throw new DeadCode();
	}

	@Override
	public int converse(final Context context) {
		properties();
		switch (context.syntax.converseDirection) {
			case UP:
				return -(int) node().getLayoutY() - (int) properties.height;
			case DOWN:
				return (int) node().getLayoutY();
			case LEFT:
				return -(int) node().getLayoutX() - (int) properties.width;
			case RIGHT:
				return (int) node().getLayoutX();
		}
		throw new DeadCode();
	}

	@Override
	public int transverse(final Context context) {
		properties();
		switch (context.syntax.transverseDirection) {
			case UP:
				return -(int) node().getLayoutY() - (int) properties.height;
			case DOWN:
				return (int) node().getLayoutY();
			case LEFT:
				return -(int) node().getLayoutX() - (int) properties.width;
			case RIGHT:
				return (int) node().getLayoutX();
		}
		throw new DeadCode();
	}

	public Vector position(final Context context) {
		properties();
		final Bounds bounds = node().getLayoutBounds();
		int converse = 0;
		int transverse = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				converse = -(int) node().getLayoutY() - (int) properties.height;
				break;
			case DOWN:
				converse = (int) node().getLayoutY();
				break;
			case LEFT:
				converse = -(int) node().getLayoutX() - (int) properties.width;
				break;
			case RIGHT:
				converse = (int) node().getLayoutX();
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				transverse = -(int) node().getLayoutY() - (int) properties.height;
				break;
			case DOWN:
				transverse = (int) node().getLayoutY();
				break;
			case LEFT:
				transverse = -(int) node().getLayoutX() - (int) properties.width;
				break;
			case RIGHT:
				transverse = (int) node().getLayoutX();
				break;
		}
		return new com.zarbosoft.merman.editor.visual.Vector(converse, transverse);
	}

	public static class TransitionSmoothOut extends Transition {
		private final Node node;
		private final Double diffX;
		private final Double diffY;

		{
			setCycleDuration(javafx.util.Duration.millis(200));
		}

		TransitionSmoothOut(final Node node, final Double diffX, final Double diffY) {
			this.node = node;
			this.diffX = diffX;
			this.diffY = diffY;
		}

		@Override
		protected void interpolate(final double frac) {
			final double frac2 = Math.pow(1 - frac, 3);
			if (diffX != null)
				node.setTranslateX(-frac2 * diffX);
			if (diffY != null)
				node.setTranslateY(-frac2 * diffY);
		}
	}

	@Override
	public void setTransverse(final Context context, final int transverse, final boolean animate) {
		properties();
		Integer x = null;
		Integer y = null;
		switch (context.syntax.transverseDirection) {
			case UP:
				y = -transverse - (int) properties.height;
				break;
			case DOWN:
				y = transverse;
				break;
			case LEFT:
				x = -transverse - (int) properties.width;
				break;
			case RIGHT:
				x = transverse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(node(), x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(node(), null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	@Override
	public void setConverse(final Context context, final int converse, final boolean animate) {
		properties();
		Integer x = null;
		Integer y = null;
		switch (context.syntax.converseDirection) {
			case UP:
				y = -converse - (int) properties.height;
				break;
			case DOWN:
				y = converse;
				break;
			case LEFT:
				x = -converse - (int) properties.width;
				break;
			case RIGHT:
				x = converse;
				break;
		}
		if (x != null) {
			if (animate)
				new TransitionSmoothOut(node(), x - node().getLayoutX(), null).play();
			node().setLayoutX(x);
		} else {
			if (animate)
				new TransitionSmoothOut(node(), null, y - node().getLayoutY()).play();
			node().setLayoutY(y);
		}
	}

	@Override
	public void setPosition(
			final Context context, final Vector vector, final boolean animate
	) {
		properties();
		int x = 0;
		int y = 0;
		switch (context.syntax.converseDirection) {
			case UP:
				y = -vector.converse - (int) properties.height;
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = -vector.converse - (int) properties.width;
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (context.syntax.transverseDirection) {
			case UP:
				y = -vector.transverse - (int) properties.height;
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = -vector.transverse - (int) properties.width;
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		if (animate)
			new TransitionSmoothOut(node(), x - node().getLayoutX(), y - node().getLayoutY()).play();
		node().setLayoutX(x);
		node().setLayoutY(y);
	}

	public void flush() {
	}

}
