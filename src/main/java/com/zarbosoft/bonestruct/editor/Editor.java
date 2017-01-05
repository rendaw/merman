package com.zarbosoft.bonestruct.editor;

import com.google.common.collect.ImmutableSet;
import com.zarbosoft.bonestruct.editor.luxem.Luxem;
import com.zarbosoft.bonestruct.editor.model.Document;
import com.zarbosoft.bonestruct.editor.model.Syntax;
import com.zarbosoft.bonestruct.editor.model.front.FrontConstantPart;
import com.zarbosoft.bonestruct.editor.visual.*;
import com.zarbosoft.bonestruct.editor.visual.nodes.VisualNode;
import com.zarbosoft.bonestruct.editor.visual.nodes.parts.ArrayVisualNode;
import com.zarbosoft.luxemj.grammar.Node;
import com.zarbosoft.pidgoon.InvalidStream;
import com.zarbosoft.pidgoon.events.Parse;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Editor {
	private final Context context;
	private final Pane visual;

	public Editor(final Consumer<IdleTask> addIdle, final String path) {
		Luxem.grammar(); // Make sure the luxem grammar is loaded beforehand so the new resource stream doesn't get closed by that resource stream
		final Syntax luxemSyntax;
		try (
				InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("luxem.syntax")
		) {
			luxemSyntax = Syntax.loadSyntax(stream);
		} catch (final IOException e) {
			throw new UncheckedIOException(e);
		}/* catch (final GrammarTooUncertain e) {
			final PrintWriter out = Helper.uncheck(() -> new PrintWriter("syntax-ambiguous.csv", "UTF-8"));
			List<ParseContext.AmbiguitySample> samples = new ArrayList<>();
			BranchingStack<ParseContext.AmbiguitySample> stack = e.context.ambiguityHistory;
			while (stack != null) {
				samples.add(stack.top());
				stack = stack.pop();
			}
			samples = Lists.reverse(samples);
			samples.forEach(s -> out.format("%d,%s,%d,%d\n", s.step, s.position, s.ambiguity, s.duplicates));
			out.close();
			throw e;
		}*/
		//final Document doc = luxemSyntax.load("[[dog, dog, dog, dog, dog, dogdogdog, dog, dog, dog]],");
		final Document doc = luxemSyntax.load("[{getConverse: 47,transverse:{ar:[2,9,13]},},[atler]]");
		//final Document doc = luxemSyntax.load("{analogue:bolivar}");
		final Wall wall = new Wall();
		context = new Context(luxemSyntax, doc, addIdle, wall);
		final ArrayVisualNode root =
				new ArrayVisualNode(context, doc.top, ImmutableSet.of(new VisualNode.PartTag("root"))) {

					@Override
					protected boolean tagLast() {
						return false;
					}

					@Override
					protected boolean tagFirst() {
						return false;
					}

					@Override
					protected Map<String, Node> getHotkeys() {
						return doc.syntax.rootHotkeys;
					}

					@Override
					protected List<FrontConstantPart> getPrefix() {
						return doc.syntax.rootPrefix;
					}

					@Override
					protected List<FrontConstantPart> getSeparator() {
						return doc.syntax.rootSeparator;
					}

					@Override
					protected List<FrontConstantPart> getSuffix() {
						return doc.syntax.rootSuffix;
					}
				};
		context.root(root);
		this.visual = new Pane();
		visual.setBackground(new Background(new BackgroundFill(context.syntax.background, null, null)));
		context.background = new Group();
		visual.getChildren().add(context.background);
		visual.getChildren().add(wall.visual);
		visual.setOnMouseExited(event -> {
			if (context.hoverIdle != null) {
				context.hoverIdle.point = null;
			} else if (context.hover != null) {
				context.hover.clear(context);
				context.hover = null;
				context.hoverBrick = null;
			}
		});
		visual.setOnMouseMoved(event -> {
			if (context.hoverIdle == null) {
				context.hoverIdle = context.new HoverIdle(context);
				addIdle.accept(context.hoverIdle);
			}
			context.hoverIdle.point = context
					.sceneToVector(visual, event.getX(), event.getY())
					.add(new Vector(-context.syntax.padConverse, -context.syntax.padTransverse));
		});
		final ChangeListener<Number> converseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			final int oldValue2 = (int) oldValue.doubleValue();
			//System.out.format("conv window size change: %d to %d\n", oldValue2, newValue2);
			context.edge = Math.max(0, newValue2 - doc.syntax.padConverse * 2);
			if (newValue2 < oldValue2) {
				wall.idleCompact(context);
			} else if (newValue2 > oldValue2) {
				wall.idleExpand(context);
			}
		};
		final ChangeListener<Number> transverseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue2 = (int) newValue.doubleValue();
			context.transverseEdge = Math.max(0, newValue2 - doc.syntax.padTransverse * 2);
		};
		switch (doc.syntax.converseDirection) {
			case UP:
				wall.visual.setLayoutY(-doc.syntax.padConverse);
				context.background.setLayoutY(-doc.syntax.padConverse);
				break;
			case DOWN:
				wall.visual.setLayoutY(doc.syntax.padConverse);
				context.background.setLayoutY(doc.syntax.padConverse);
				break;
			case LEFT:
				wall.visual.setLayoutX(-doc.syntax.padConverse);
				context.background.setLayoutX(-doc.syntax.padConverse);
				break;
			case RIGHT:
				wall.visual.setLayoutX(doc.syntax.padConverse);
				context.background.setLayoutX(doc.syntax.padConverse);
				break;
		}
		switch (doc.syntax.transverseDirection) {
			case UP:
				wall.visual.setLayoutY(-doc.syntax.padTransverse);
				context.background.setLayoutY(-doc.syntax.padTransverse);
				break;
			case DOWN:
				wall.visual.setLayoutY(doc.syntax.padTransverse);
				context.background.setLayoutY(doc.syntax.padTransverse);
				break;
			case LEFT:
				wall.visual.setLayoutX(-doc.syntax.padTransverse);
				context.background.setLayoutX(-doc.syntax.padTransverse);
				break;
			case RIGHT:
				wall.visual.setLayoutX(doc.syntax.padTransverse);
				context.background.setLayoutX(doc.syntax.padTransverse);
				break;
		}
		switch (doc.syntax.converseDirection) {
			case UP:
			case DOWN:
				/*
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				*/
				visual.heightProperty().addListener(converseSizeListener);
				//converseSizeListener.changed(null, Double.MAX_VALUE, scene.heightProperty().getValue());
				visual.widthProperty().addListener(transverseSizeListener);
				break;
			case LEFT:
			case RIGHT:
				/*
				scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
				scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
				*/
				visual.widthProperty().addListener(converseSizeListener);
				visual.heightProperty().addListener(transverseSizeListener);
				break;
		}
	}

	public Pane getVisual() {
		return visual;
	}

	public void handleKey(final KeyEvent event) {
		if (context.hotkeyParse == null) {
			context.hotkeyParse = new Parse<Context.Action>().grammar(context.hotkeyGrammar).parse();
		}
		final Keyboard.Event keyEvent = new Keyboard.Event(Keyboard.fromJFX(event.getCode()),
				event.isControlDown(),
				event.isAltDown(),
				event.isShiftDown()
		);
		if (context.hotkeySequence.isEmpty())
			context.hotkeySequence += keyEvent.toString();
		else
			context.hotkeySequence += ", " + keyEvent.toString();
		boolean clean = false;
		try {
			context.hotkeyParse = context.hotkeyParse.push(keyEvent, context.hotkeySequence);
		} catch (final InvalidStream e) {
			clean = true;
		}
		final Context.Action action = context.hotkeyParse.finish();
		if (action != null) {
			clean = true;
			action.run(context);
		}
		if (clean) {
			context.hotkeySequence = "";
			context.hotkeyParse = null;
			context.selection.receiveText(context, event.getText());
		}
	}
}
