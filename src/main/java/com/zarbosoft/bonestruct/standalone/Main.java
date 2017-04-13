package com.zarbosoft.bonestruct.standalone;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.history.History;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.PriorityQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Main extends Application {
	public static void main(final String[] args) {
		launch(args);
	}

	private final Logger logger = LoggerFactory.getLogger("main");
	private final ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	private boolean idlePending = false;
	private ScheduledFuture<?> idleTimer = null;
	private final PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();
	private Path filename;

	@FunctionalInterface
	private interface Wrappable {
		void run() throws Exception;
	}

	private void wrap(final Wrappable runnable) {
		try {
			runnable.run();
		} catch (final Exception e) {
			logger.error("Exception passed sieve.", e);
			final Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
			alert.showAndWait();
		}
	}

	@Override
	public void start(final Stage primaryStage) {
		/*
		TODO
		add mouse scroll buttons to input patterns
		details
		editing, actions for everythinga
		save
		modes (global) - modules can create too
			ex: nav/edit, edit, nav, debug, refactor
			style/tag based on mode
		order of operations, conditional front elements

		plugin add colored bar to side if dirty
		banner background
		windowing
		persistent history
		selection history
		modules from dir
		//syntaxes from dir && install reference syntaxes automatically
		document distributing modules
		document distributions
		lua actions
		 */
		final EditorGlobal global = new EditorGlobal();
		{
			final Path logRoot = global.appDirs.user_log_dir(true);
			uncheck(() -> Files.createDirectories(logRoot));
			final Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
			final LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
			final PatternLayoutEncoder ple = new PatternLayoutEncoder();
			ple.setPattern("%date %level %logger{10} [%file:%line] %msg%n");
			ple.setContext(lc);
			ple.start();
			final FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
			fileAppender.setFile(logRoot.resolve("log.txt").toString());
			fileAppender.setEncoder(ple);
			fileAppender.setContext(lc);
			fileAppender.start();
		}
		global.initializeFilesystem();
		if (getParameters().getUnnamed().isEmpty())
			throw new IllegalArgumentException("Must specify a filename as first argument.");
		filename = Paths.get(getParameters().getUnnamed().get(0));
		final History history = new History();
		final Editor editor =
				new Editor(global, this::addIdle, getParameters().getUnnamed().get(0), ImmutableList.of(new Action() {
					@Override
					public void run(final Context context) {
						Platform.exit();
					}

					@Override
					public String getName() {
						return "quit";
					}
				}), history);
		final HBox filesystemLayout = new HBox();
		final TextField filenameEntry = new TextField(getParameters().getUnnamed().get(0));
		filesystemLayout.getChildren().add(filenameEntry);
		final Button save = new Button("Save");
		filesystemLayout.getChildren().add(save);
		final Button rename = new Button("Rename");
		filesystemLayout.getChildren().add(rename);
		final Button saveAs = new Button("Save as");
		filesystemLayout.getChildren().add(saveAs);
		final Button backup = new Button("Back-up");
		filesystemLayout.getChildren().add(backup);
		history.addModifiedStateListener(modified -> {
			if (modified) {
				save.getStyleClass().add("modified");
				rename.getStyleClass().add("modified");
				saveAs.getStyleClass().add("modified");
				backup.getStyleClass().add("modified");
			} else {
				save.getStyleClass().remove("modified");
				rename.getStyleClass().remove("modified");
				saveAs.getStyleClass().remove("modified");
				backup.getStyleClass().remove("modified");
			}
		});
		final ChangeListener<String> alignFilesystemLayout = new ChangeListener<String>() {
			@Override
			public void changed(
					final ObservableValue<? extends String> observable, final String oldValue, final String newValue
			) {
				wrap(() -> {
					if (newValue.equals(filename)) {
						save.setVisible(true);
						rename.setVisible(false);
						saveAs.setVisible(false);
						backup.setVisible(false);
					} else {
						save.setVisible(false);
						rename.setVisible(true);
						saveAs.setVisible(true);
						backup.setVisible(true);
					}
				});
			}
		};
		filenameEntry.textProperty().addListener(alignFilesystemLayout);
		save.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(() -> {
					editor.save(filename);
				});
			}
		});
		rename.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(() -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest)) {
						final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
								"File exists, overwrite?",
								ButtonType.YES,
								ButtonType.NO
						);
						confirm.showAndWait();
						if (confirm.getResult() != ButtonType.YES)
							return;
					}
					editor.save(filename);
					Files.move(filename, dest);
					filename = Paths.get(filenameEntry.getText());
					alignFilesystemLayout.changed(null, null, filename.toString());
				});
			}
		});
		saveAs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(() -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest)) {
						final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
								"File exists, overwrite?",
								ButtonType.YES,
								ButtonType.NO
						);
						confirm.showAndWait();
						if (confirm.getResult() != ButtonType.YES)
							return;
					}
					filename = dest;
					editor.save(dest);
					alignFilesystemLayout.changed(null, null, filename.toString());
				});
			}
		});
		backup.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(() -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest)) {
						final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
								"File exists, overwrite?",
								ButtonType.YES,
								ButtonType.NO
						);
						confirm.showAndWait();
						if (confirm.getResult() != ButtonType.YES)
							return;
					}
					editor.save(dest);
					filenameEntry.setText(filename.toString());
				});
			}
		});
		final VBox mainLayout = new VBox();
		mainLayout.getChildren().add(filesystemLayout);
		mainLayout.getChildren().add(editor.getVisual());
		final Scene scene = new Scene(mainLayout, 300, 275);
		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent t) {
				if (history.isModified()) {
					final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
							"File exists, overwrite?",
							ButtonType.YES,
							ButtonType.NO
					);
					confirm.showAndWait();
					if (confirm.getResult() != ButtonType.YES) {
						t.consume();
						return;
					}
				}
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
						wrap(() -> {
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
					});
				}, 0, 50, TimeUnit.MILLISECONDS);
			} catch (final RejectedExecutionException e) {
				// Happens on unhover when window closes to shutdown
			}
		}
	}
}
