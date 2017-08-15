package com.zarbosoft.bonestruct.standalone;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.google.common.collect.ImmutableList;
import com.zarbosoft.bonestruct.document.Document;
import com.zarbosoft.bonestruct.editor.*;
import com.zarbosoft.bonestruct.editor.display.javafx.JavaFXDisplay;
import com.zarbosoft.bonestruct.editor.history.History;
import com.zarbosoft.bonestruct.syntax.Syntax;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
	private boolean idlePending = false;
	private ScheduledFuture<?> idleTimer = null;
	private final PriorityQueue<IdleTask> idleQueue = new PriorityQueue<>();
	private Path filename;
	private Stage stage;
	private JavaFXDisplay display;

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

	@Override
	public void start(final Stage primaryStage) {
		stage = primaryStage;
		/*
		TODO
		//add mouse scroll buttons to input patterns
		//	make select_hovered an action
		//button down + up hotkey events
		//details
		//editing, actions for everythinga
		//save
		//modes (global) - modules can create too
		//	ex: nav/edit, edit, nav, debug, refactor
		//	style/tag based on mode
		//order of operations, conditional front elements
		//windowing, depth indicator
		//	window nodes, not values (except root valuearray)
		//	atom, core, mote, basis; atom -> core?
		//	actually, window visuals, so the above 2 lines moot probably
		//ellipsis with depth score type param
		//__ ellipsis is the opposite of windowing
		//__ if selecting ellipsized visual, move the window until the ellipsis score is under the threshold
		//__ change rootAlignments to just root, recursively update ellipsis values as well and stop after ellipsizement
		//primitive compact/expand
		//window indicator -> change global tags (state windowed/nonwindowed)
		//indicators -> plugin that shows/hides indicators by tag
		//gap handle conditional front elements
		//non printable & front array/atom placeholder character as syntax param
		//gap preview details styling
		//	columns: 1:preview 2:type id
		//gap choice selection
		//gap preview styling
		//hotkey preview details
		//	columns: 1:rule 2:action name
		//fix save modifications on empty doc
		//test windowing
		//add window up/down actions
		//fix details position
		//fix -> up arrow if not bound
		//fix cursor (overlay padding + scroll?); still duplicating
		//fix banner position
		//syntaxes from dir && install reference syntaxes automatically
		//separate lrtb padding
		//box lrtb padding
		//remove part tags (explicit only)
		//add prefix/suffix actions in atom/array vis
		//banner and details padding
		//primitive leadFirst
		//store array/primitive leadFirst in sel saveState
		//show details on gap select if nonempty
		//arbitrary key modifiers
		//fix dialogs
		//add actions for choice 1-9,0
		//abstract xy origin for alternative converse/transverse directions, reduce window resize listeners
		//test brick layout
		//rename break -> split
		//fix hotkey modifiers
		//fix indent/breaking
		//fix hover
		//fix compact (root array)
		//cornerstone on wrong element (visual array selectup)
		//translation bounce on array element enter/exit
		// X indicators in overlay
		// X document extends atom?
		//no more visualpart?
		//new line in primitive
		//_lua actions
		//fix concensus in record
		//fix record value reduce doesn't trigger expand?
		//fix disappearing obboxes
		//fix wrong cursor pos on expand - can't reproduce

		//mouse peek
		//scroll up/down; scroll large up/down; reset scroll
		//I don't think gaps are allowed everywhere - direct references should be substituted with groups containing 1 type + gap types
		//	+ gaps should be added to all user groups
		//Disallow referring to the root type somehow
		save -> still dirty popup
		readme
		doc on tagging
		//syntax documenter
		//action documenter
		//	action name as a static method?
		//	or java ast parser to extract actions? description as block comment?
		action to select part directly
		compact/expand all with same priority together
		clean up luxem syntax (colors, etc)
		improve hover type info
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
		stage.setTitle(filename.getFileName().toString());
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
		final Editor editor = new Editor(syntax, doc, display, this::addIdle, path, history);
		editor.addActions(this, ImmutableList.of(new ActionQuit(), new ActionDebug()));
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
					stage.setTitle(filename.getFileName().toString());
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
					stage.setTitle(filename.getFileName().toString());
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
		final Scene scene = new Scene(mainLayout, 300, 275);
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
		stage.show();
		editor.focus();
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
									//System.out.format("Idle stopping at %d\n", i);
									break;
								} else {
									if (top.run())
										addIdle(top);
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

	private abstract static class ActionBase extends Action {
		public static String group() {
			return "application";
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
