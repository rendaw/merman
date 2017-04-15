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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
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
	private Stage stage;

	@FunctionalInterface
	private interface Wrappable {
		void run() throws Exception;
	}

	private void wrap(final Window top, final Wrappable runnable) {
		try {
			runnable.run();
		} catch (final Exception e) {
			logger.error("Exception passed sieve.", e);
			final Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
			alert.initModality(Modality.APPLICATION_MODAL);
			alert.initOwner(top);
			alert.setResizable(true);
			alert.showAndWait();
		}
	}

	private boolean confirmOverwrite(final Window top) {
		final Alert confirm =
				new Alert(Alert.AlertType.CONFIRMATION, "File exists, overwrite?", ButtonType.YES, ButtonType.NO);
		confirm.initModality(Modality.APPLICATION_MODAL);
		confirm.initOwner(top);
		confirm.setResizable(true);
		confirm.showAndWait();
		if (confirm.getResult() != ButtonType.YES)
			return false;
		return true;
	}

	@Override
	public void start(final Stage primaryStage) {
		stage = primaryStage;
		/*
		TODO
		add mouse scroll buttons to input patterns
		button down + up hotkey events
		details
		editing, actions for everythinga
		//save
		//modes (global) - modules can create too
		//	ex: nav/edit, edit, nav, debug, refactor
		//	style/tag based on mode
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
		stage.setTitle(filename.getFileName().toString());
		final History history = new History();
		final Editor editor =
				new Editor(global, this::addIdle, Paths.get(getParameters().getUnnamed().get(0)), history);
		editor.addActions(this, ImmutableList.of(new Action() {
			@Override
			public void run(final Context context) {
				Platform.exit();
			}

			@Override
			public String getName() {
				return "quit";
			}
		}));
		final HBox filesystemLayout = new HBox();
		filesystemLayout.setPadding(new Insets(3, 2, 3, 2));
		filesystemLayout.setSpacing(5);
		final TextField filenameEntry = new TextField(getParameters().getUnnamed().get(0));
		filesystemLayout.getChildren().add(filenameEntry);
		HBox.setHgrow(filenameEntry, Priority.ALWAYS);
		final Button save = new Button("Save") {
		};
		final Button rename = new Button("Rename");
		final Button saveAs = new Button("Save as");
		final Button backup = new Button("Back-up");
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
		final ChangeListener<String> alignFilesystemLayout = new ChangeListener<>() {
			@Override
			public void changed(
					final ObservableValue<? extends String> observable, final String oldValue, final String newValue
			) {
				wrap(stage.getOwner(), () -> {
					if (newValue.equals(filename.toString())) {
						filesystemLayout.getChildren().removeAll(ImmutableList.of(rename, saveAs, backup));
						filesystemLayout.getChildren().add(save);
					} else {
						filesystemLayout.getChildren().remove(save);
						filesystemLayout.getChildren().addAll(ImmutableList.of(rename, saveAs, backup));
					}
				});
			}
		};
		filenameEntry.textProperty().addListener(alignFilesystemLayout);
		alignFilesystemLayout.changed(null, null, filename.toString());
		filenameEntry.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (save.isVisible())
					save.getOnAction().handle(null);
			}
		});
		save.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(stage.getOwner(), () -> {
					editor.save(filename);
					editor.getVisual().requestFocus();
				});
			}
		});
		rename.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(stage.getOwner(), () -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest) && !confirmOverwrite(stage.getOwner()))
						return;
					editor.save(filename);
					Files.move(filename, dest);
					filename = Paths.get(filenameEntry.getText());
					stage.setTitle(filename.getFileName().toString());
					alignFilesystemLayout.changed(null, null, filename.toString());
					editor.getVisual().requestFocus();
				});
			}
		});
		saveAs.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(stage.getOwner(), () -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest) && !confirmOverwrite(stage.getOwner()))
						return;
					filename = dest;
					stage.setTitle(filename.getFileName().toString());
					editor.save(dest);
					alignFilesystemLayout.changed(null, null, filename.toString());
					editor.getVisual().requestFocus();
				});
			}
		});
		backup.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				wrap(stage.getOwner(), () -> {
					final Path dest = Paths.get(filenameEntry.getText());
					if (Files.exists(dest) && !confirmOverwrite(stage.getOwner()))
						return;
					editor.save(dest);
					filenameEntry.setText(filename.toString());
					editor.getVisual().requestFocus();
				});
			}
		});
		final VBox mainLayout = new VBox();
		mainLayout.getChildren().add(filesystemLayout);
		mainLayout.getChildren().add(editor.getVisual());
		VBox.setVgrow(editor.getVisual(), Priority.ALWAYS);
		final Scene scene = new Scene(mainLayout, 300, 275);
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<>() {
			@Override
			public void handle(final WindowEvent t) {
				if (history.isModified()) {
					final Alert confirm = new Alert(
							Alert.AlertType.CONFIRMATION,
							"File has unsaved changes. Do you still want to quit?",
							ButtonType.YES,
							ButtonType.NO
					);
					confirm.initOwner(stage.getOwner());
					confirm.initModality(Modality.APPLICATION_MODAL);
					confirm.setResizable(true);
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
		stage.show();
		editor.getVisual().requestFocus();
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
						wrap(stage.getOwner(), () -> {
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
