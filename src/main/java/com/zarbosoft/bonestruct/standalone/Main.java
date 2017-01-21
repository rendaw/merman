package com.zarbosoft.bonestruct.standalone;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.Editor;
import com.zarbosoft.bonestruct.editor.changes.History;
import com.zarbosoft.bonestruct.editor.visual.Context;
import com.zarbosoft.bonestruct.editor.visual.IdleTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.PriorityQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main extends Application {
	private final IdleTask idleCompact = null;

	public static void main(final String[] args) {
		launch(args);
	}

	ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	boolean idlePending = false;
	ScheduledFuture<?> idleTimer = null;
	public PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();

	@Override
	public void start(final Stage primaryStage) {
		/*
		TODO
		scrolling = jumpTo
			center brick + center transverse
				base new courses and placement on center
				center = begin brick of selection
				if no brick, clear and regenerate bricks from createFirstBrick on selection;
					center transverse = 0
			if course end > screen end, move visual (backgroudn/fg) forward
			if course start < screen start, move visual back
			(if no brick instantaneous visual move)
		banner
			selection change sets visual attachment
			visual brick create sets brick (course) attachment
			on course move,
			transition no data -> data
				smooth increase fixture
		details
		windowing
		editing, actions for everythinga
		global context
		save
		plugins
		persistent history
		selection history
		 */
		/*
		if (getParameters().getUnnamed().isEmpty())
			throw new IllegalArgumentException("Must specify a filename as first argument.");
			*/
		final History history = new History();
		//final Editor editor = new Editor(this::addIdle, getParameters().getUnnamed().get(0));
		final Editor editor = new Editor(this::addIdle, "todo", ImmutableList.of(new Context.Action() {
			@Override
			public void run(final Context context) {
				Platform.exit();
			}

			@Override
			public String getName() {
				return "quit";
			}
		}), history);
		final Scene scene = new Scene(editor.getVisual(), 300, 275);
		scene.setOnKeyPressed(event -> {
			editor.handleKey(event);
		});
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent t) {
				editor.destroy();
				worker.shutdown();
			}
		});
	}

	private void addIdle(final IdleTask task) {
		idleQueue.add(task);
		if (idleTimer == null) {
			try {
				idleTimer = worker.scheduleWithFixedDelay(() -> {
					//System.out.println("idle timer");
					if (idlePending)
						return;
					idlePending = true;
					Platform.runLater(() -> {
						//System.out.println(String.format("idle timer inner: %d", idleQueue.size()));
						// TODO measure pending event backlog, adjust batch size to accomodate
						// by proxy? time since last invocation?
						for (int i = 0; i < 1000; ++i) { // Batch
							final IdleTask top = idleQueue.poll();
							if (top == null) {
								idleTimer.cancel(false);
								idleTimer = null;
								System.out.format("Idle stopping at %d\n", i);
								break;
							} else {
								top.run();
							}
						}
						//System.out.format("Idle break at g i %d\n", idleCount);
						idlePending = false;
					});
				}, 0, 50, TimeUnit.MILLISECONDS);
			} catch (final RejectedExecutionException e) {
				// Happens on unhover when window closes to shutdown
			}
		}
	}
}
