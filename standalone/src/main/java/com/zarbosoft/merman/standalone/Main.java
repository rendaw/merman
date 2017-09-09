package com.zarbosoft.merman.standalone;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.document.Document;
import com.zarbosoft.merman.editor.*;
import com.zarbosoft.merman.editor.display.javafx.JavaFXDisplay;
import com.zarbosoft.merman.editor.history.History;
import com.zarbosoft.merman.syntax.Syntax;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

import static com.zarbosoft.rendaw.common.Common.last;
import static com.zarbosoft.rendaw.common.Common.uncheck;

public class Main extends Application {

	public static void main(final String[] args) {
		launch(args);
	}

	private final Logger logger = LoggerFactory.getLogger("main");
	private final ScheduledThreadPoolExecutor worker = new ScheduledThreadPoolExecutor(1);
	private boolean iterationPending = false;
	private ScheduledFuture<?> iterationTimer = null;
	private IterationContext iterationContext = null;
	private final PriorityQueue<IterationTask> iterationQueue = new PriorityQueue<>();
	private Path filename;
	private Stage stage;
	private JavaFXDisplay display;
	private Editor editor;

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
			alert
					.getDialogPane()
					.getChildren()
					.stream()
					.filter(node -> node instanceof Label)
					.forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
			alert.showAndWait();
		}
	}

	private boolean confirmOverwrite(final Window top) {
		return confirmDialog(stage, "File exists, overwrite?");
	}

	public static boolean confirmDialog(final Stage stage, final String text) {
		final Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.NO, ButtonType.YES);
		confirm.initOwner(stage.getOwner());
		confirm.initModality(Modality.APPLICATION_MODAL);
		confirm.setResizable(true);
		confirm
				.getDialogPane()
				.getChildren()
				.stream()
				.filter(node -> node instanceof Label)
				.forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
		confirm.showAndWait();
		return confirm.getResult() == ButtonType.YES;
	}

	private void setTitle() {
		stage.setTitle(String.format("%s - merman", filename.getFileName().toString()));
	}

	@Override
	public void start(final Stage primaryStage) {
		stage = primaryStage;
		/*
		TODO
		readme
		doc on tagging
		//syntax documenter
		//action documenter
		//	action name as a static method?
		//	or java ast parser to extract actions? description as block comment?
		action to select part directly
		compact/expand all with same priority together
		//clean up luxem syntax (colors, etc)
		//improve hover type info
		publish all dependencies
		hn/reddit

		long range goals
		_sed (led) substitution
		_modules from dir
		_document distributing modules
		_document distributions
		_layout templates for complete details + banner styling
		_vertical text
		_toc plugin
		_persistent history ~ changes relative to back, watch syntax and autoreload
		_better visibility into action bindings
		_better syntax lua parse error messages
		_limit bricks to widget boundaries +- 1 page or so
			_when brick created and not split, if any atom with a brick in the new brick's course has lower space priority and is compact, compact this atom
			_keep compact status in atom?  when creating a brick, make it start compacted
		_pidgoon references ("mold"?) for things like swap sugar

		?
		move scroll to wall with listener for banner/details?
		display methods take display, not context; add default methods to turn context -> display
		add array prefix/suffix gaps (?)
		store selection position in visual? restore on selectDown
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
		if (!Files.exists(filename.toAbsolutePath().normalize().getParent()))
			throw new IllegalArgumentException(String.format(
					"Directory of specified file must exist.\nAbsolute path: %s",
					filename.toAbsolutePath().normalize()
			));
		setTitle();
		final History history = new History();
		final Path path = Paths.get(getParameters().getUnnamed().get(0));
		final String extension = last(path.getFileName().toString().split("\\."));
		final Syntax syntax = global.getSyntax(extension);
		final Document doc;
		if (Files.exists(path))
			doc = uncheck(() -> syntax.load(path));
		else
			doc = syntax.create();
		this.display = new JavaFXDisplay(syntax);
		editor = new Editor(syntax, doc, display, this::addIdle, path, history, new SimpleClipboardEngine());
		editor.addActions(this, ImmutableList.of(new ActionSave(), new ActionQuit(), new ActionDebug()));
		final HBox filesystemLayout = new HBox();
		filesystemLayout.setPadding(new Insets(3, 2, 3, 2));
		filesystemLayout.setSpacing(5);
		final TextField filenameEntry = new TextField(getParameters().getUnnamed().get(0));
		filesystemLayout.getChildren().add(filenameEntry);
		HBox.setHgrow(filenameEntry, Priority.ALWAYS);
		final Button save = new Button("Save");
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
					editor.focus();
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
					setTitle();
					alignFilesystemLayout.changed(null, null, filename.toString());
					editor.focus();
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
					setTitle();
					editor.save(dest);
					alignFilesystemLayout.changed(null, null, filename.toString());
					editor.focus();
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
					editor.focus();
				});
			}
		});
		final VBox mainLayout = new VBox();
		mainLayout.getChildren().add(filesystemLayout);
		mainLayout.getChildren().add(display.node);
		VBox.setVgrow(display.node, Priority.ALWAYS);
		final Scene scene = new Scene(mainLayout, 700, 500);
		stage.setScene(scene);
		stage.setOnCloseRequest(new EventHandler<>() {
			@Override
			public void handle(final WindowEvent t) {
				if (history.isModified()) {
					if (!confirmDialog(stage, "File has unsaved changes. Do you still want to quit?")) {
						t.consume();
						return;
					}
				}
				editor.destroy();
				worker.shutdown();
			}
		});
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/zarbosoft/merman/resources/icon48.png")));
		stage.show();
		editor.focus();
	}

	private void addIdle(final IterationTask task) {
		iterationQueue.add(task);
		if (iterationTimer == null) {
			try {
				iterationTimer = worker.scheduleWithFixedDelay(() -> {
					if (iterationPending)
						return;
					iterationPending = true;
					Platform.runLater(() -> {
						wrap(stage.getOwner(), () -> {
							try {
								final long start = System.currentTimeMillis();
								// TODO measure pending event backlog, adjust batch size to accomodate
								// by proxy? time since last invocation?
								for (int i = 0; i < 1000; ++i) {
									{
										long now = start;
										if (i % 100 == 0) {
											now = System.currentTimeMillis();
										}
										if (now - start > 500) {
											iterationContext = null;
											break;
										}
									}
									final IterationTask top = iterationQueue.poll();
									if (top == null) {
										iterationContext = null;
										break;
									} else {
										if (iterationContext == null)
											iterationContext = new IterationContext();
										if (top.run(iterationContext))
											addIdle(top);
									}
								}
							} finally {
								iterationPending = false;
							}
						});
					});
				}, 0, 50, TimeUnit.MILLISECONDS);
			} catch (final RejectedExecutionException e) {
				// Happens on unhover when window closes to shutdown
			}
		}
	}

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "application";
		}
	}

	@Action.StaticID(id = "save")
	private class ActionSave extends ActionBase {
		@Override
		public boolean run(final Context context) {
			editor.save(filename);
			return true;
		}
	}

	@Action.StaticID(id = "quit")
	private static class ActionQuit extends ActionBase {
		@Override
		public boolean run(final Context context) {
			Platform.exit();
			return true;
		}
	}

	@Action.StaticID(id = "debug")
	private static class ActionDebug extends ActionBase {
		@Override
		public boolean run(final Context context) {
			System.out.format("This is a convenient place to put a breakpoint.\n");
			return true;
		}
	}
}
