package com.zarbosoft.bonestruct.ui;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.zarbosoft.bonestruct.model.Document;
import com.zarbosoft.bonestruct.model.Node;
import com.zarbosoft.bonestruct.model.Syntax;
import com.zarbosoft.bonestruct.model.front.FrontPart;
import com.zarbosoft.bonestruct.model.front.Mark;
import com.zarbosoft.bonestruct.model.front.Primitive;
import com.zarbosoft.bonestruct.model.front.Reference;
import com.zarbosoft.luxemj.Parse;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	class ProgressBar extends Canvas {
		private GraphicsContext gc;

		ProgressBar() {
			super(300, 20);
			this.gc = this.getGraphicsContext2D();
		}
		
		public void set(double percent) {
			double mid = this.getWidth() * percent;
			gc.clearRect(mid, 0, this.getWidth() - mid, this.getHeight());
			gc.setFill(Color.BLUEVIOLET);
			gc.fillRect(0, 0, mid, this.getHeight());
		}
	}
	
	class Context {
		Syntax luxemSyntax;
		
		public Context() throws IOException {
			luxemSyntax = new Parse<Syntax>()
				.grammar(Syntax.syntaxGrammar())
				.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("luxem.syntax"));
		}
	}

	private javafx.scene.Node createPart(FrontPart prenode, Map<String, Object> data) {
		try {
			Mark node = (Mark) prenode;
			return new Text(node.value);
		} catch (ClassCastException e) {}
		try {
			Primitive node = (Primitive) prenode;
			return new Text(node.value);
		} catch (ClassCastException e) {}
		try {
			Reference node = (Reference) prenode;
			return createNode((Node) data.get(node.name));
		} catch (ClassCastException e) {}
		throw new AssertionError("Unimplemented front node type " + prenode.getClass());
	}
	
	private UINode createNode(Node node) {
		StackPane stack = new StackPane();
		Canvas canvas = new Canvas();
		stack.getChildren().add(canvas);
		HBox layout = new HBox();
		node.type.front.forEach(n -> layout.getChildren().add(createPart(n, node.data)));
		return stack;
	}
	
	private static void adjustLayout(UINode node, double edge) {
		// Try unsplit from leaves
		{
			class Level {
				UINode node;
				Iterator<UINode> iter;
				boolean pass = false;
				
				Level(UINode node) {
					this.node = node;
					iter = node.children().iterator();
				}
			}
			
			Deque<Level> stack = new ArrayDeque<>();
			
			{
				UINode at = node.parent();
				while (at != null) {
					stack.addFirst(new Level(at));
					at = at.parent();
				}
			}
			
			{
				stack.addLast(new Level(node));
				while (!stack.isEmpty()) {
					Level at = stack.getLast();
					if (at.iter.hasNext()) {
						UINode next = at.iter.next();
						stack.addLast(new Level(next));
					} else {
						stack.removeLast();
						if (at.unsplit() && !at.exceeds(edge))
							stack.getLast().pass = true;
					}
				}
			}
		}
		
		// Split from base
		{
			class Level {
				Iterator<UINode> iter;
				boolean expand = false;
				
				Level(Iterator<UINode> iter, boolean expand) {
					this.iter = iter;
					this.expand = expand;
				}
			}
			Deque<Level> stack = new ArrayDeque<>();
			
			{
				UINode at = node.parent();
				while (at != null) {
					stack.addLast(new Level(Iterators.forArray(at), false));
					at = at.parent();
				}
				stack.addLast(new Level(Iterators.forArray(node), true));
			}
			
			while (!stack.isEmpty()) {
				Level top;
				UINode at;
				try {
					top = stack.removeLast();
					at = top.iter.next();
					stack.addLast(top);
				}
				catch (NoSuchElementException e) {
					continue;
				}
				if (!at.exceeds(edge)) continue;
				at.split(edge);
				if (top.expand)
					stack.addLast(new Level(at.children().iterator(), true));
			}
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		Task<Context> task = new Task<Context>() {
			@Override
			protected Context call() throws Exception {
				return new Context();
			}
		};
		{
			primaryStage.setTitle("Bonestruct");
			GridPane grid = new GridPane();
			Text text = new Text("loading");
			grid.add(text, 0, 1);
			ProgressBar progress = new ProgressBar();
			task.progressProperty().addListener((p, o, n) -> progress.set(n.doubleValue()));
			grid.add(progress, 0, 2);
			Scene scene = new Scene(grid, 300, 275);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		new Thread(task).start();
		try {
			// 1. create blank document + wrap via size changes
			// 2. navigation
			// 3. editing
			// 4. rewrap on edit, replace
			Context context = task.get();
			Document doc = context.luxemSyntax.create();
			VBox layout = new VBox();
			UINode root = createNode(doc.root);
			layout.getChildren().add(root);
			Scene scene = new Scene(layout, 300, 275);
			scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
				adjustLayout(root, newSceneWidth.doubleValue());
			});
			adjustLayout(root, scene.widthProperty().doubleValue());
			primaryStage.setScene(scene);
		} catch (InterruptedException | ExecutionException e) {
			Text text = new Text(Throwables.getStackTraceAsString(e));
			VBox layout = new VBox();
			layout.getChildren().add(text);
			Scene scene = new Scene(layout, 300, 275);
			primaryStage.setScene(scene);
		}
	}
}
