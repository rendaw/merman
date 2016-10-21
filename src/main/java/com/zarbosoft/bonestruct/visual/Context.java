package com.zarbosoft.bonestruct.visual;

import com.zarbosoft.bonestruct.model.Document;
import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.visual.nodes.VisualNode;
import javafx.animation.Interpolator;
import javafx.geometry.Point2D;
import javafx.scene.Scene;

import java.util.function.Consumer;

public class Context {
	public static abstract class Hoverable {

		public abstract Hoverable hover(Context context, Vector point);

		public abstract void clear(Context context);
	}

	public class HoverIdle extends IdleTask {
		public Vector point = null;
		final VisualNode root;
		Context context;

		public HoverIdle(final Context context, final VisualNode root) {
			this.root = root;
			this.context = context;
		}

		@Override
		public void run() {
			if (hover != null && point == null) {
				hover.clear(context);
				hover = null;
			} else {
				final Hoverable oldHover = hover;
				hover = hover == null ? root.hover(context, point) : hover.hover(context, point);
				//System.out.format("hov c %d t %d old %s new %s\n", point.converse, point.transverse, oldHover, hover);
				if (hover != oldHover) {
					if (oldHover != null)
						oldHover.clear(context);
					addIdle(this);
				} else
					hoverIdle = null;
			}
		}
	}

	public final Syntax syntax;
	public final Document document;
	public int edge = 0;
	public int transverseEdge = 0;
	public Hoverable hover;
	public VisualNode select;
	public HoverIdle hoverIdle;

	public Context(
			final Syntax syntax, final Document document, final Consumer<IdleTask> addIdle
	) {
		this.syntax = syntax;
		this.document = document;
		this.addIdle = addIdle;
	}

	private final Consumer<IdleTask> addIdle;

	public void addIdle(final IdleTask task) {
		this.addIdle.accept(task);
	}

	static class TheInterpolator extends Interpolator {
		@Override
		protected double curve(double t) {
			t = t * 2;
			if (t * 2 < 1)
				return Math.pow(t, 3) / 2;
			else
				return Math.pow(t - 1, 3) / 2 + 1;
		}
	}

	TheInterpolator interpolator = new TheInterpolator();

	public Vector sceneToVector(final Scene scene, final double x, final double y) {
		int converse = 0;
		int transverse = 0;
		switch (syntax.converseDirection) {
			case UP:
				converse = (int) (scene.heightProperty().doubleValue() - y);
				break;
			case DOWN:
				converse = (int) y;
				break;
			case LEFT:
				converse = (int) (scene.widthProperty().doubleValue() - x);
				break;
			case RIGHT:
				converse = (int) x;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				transverse = (int) (scene.heightProperty().doubleValue() - x);
				break;
			case DOWN:
				transverse = (int) y;
				break;
			case LEFT:
				transverse = (int) (scene.widthProperty().doubleValue() - x);
				break;
			case RIGHT:
				transverse = (int) x;
				break;
		}
		return new Vector(converse, transverse);
	}

	public Point2D toScreen(final Vector source) {
		double x = 0, y = 0;
		switch (syntax.converseDirection) {
			case UP:
				y = edge - source.converse;
				break;
			case DOWN:
				y = source.converse;
				break;
			case LEFT:
				x = edge - source.converse;
				break;
			case RIGHT:
				x = source.converse;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				y = transverseEdge - source.transverse;
				break;
			case DOWN:
				y = source.transverse;
				break;
			case LEFT:
				x = transverseEdge - source.transverse;
				break;
			case RIGHT:
				x = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	public Point2D toScreenSpan(final Vector source) {
		double x = 0, y = 0;
		switch (syntax.converseDirection) {
			case UP:
			case DOWN:
				x = source.transverse;
				y = source.converse;
				break;
			case LEFT:
			case RIGHT:
				x = source.converse;
				y = source.transverse;
				break;
		}
		return new Point2D(x, y);
	}

	public void translate(final javafx.scene.Node node, final Vector vector) {
		int x = 0;
		int y = 0;
		switch (syntax.converseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + vector.converse;
				break;
			case DOWN:
				y = vector.converse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + vector.converse;
				break;
			case RIGHT:
				x = vector.converse;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				y = (int) node.getLayoutBounds().getHeight() + vector.transverse;
				break;
			case DOWN:
				y = vector.transverse;
				break;
			case LEFT:
				x = (int) node.getLayoutBounds().getWidth() + vector.transverse;
				break;
			case RIGHT:
				x = vector.transverse;
				break;
		}
		/*
		final TranslateTransition translation = new TranslateTransition(Duration.seconds(1), node);
		translation.setInterpolator(interpolator);
		translation.setToX(x);
		translation.setToY(y);
		translation.play();
		*/
		node.setLayoutX(x);
		node.setLayoutY(y);
	}
}
