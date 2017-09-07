package com.zarbosoft.merman.editor.display.javafx;

import com.google.common.collect.ImmutableList;
import com.zarbosoft.merman.editor.Context;
import com.zarbosoft.merman.editor.display.*;
import com.zarbosoft.merman.editor.hid.HIDEvent;
import com.zarbosoft.merman.editor.visual.Vector;
import com.zarbosoft.merman.modules.hotkeys.Key;
import com.zarbosoft.merman.syntax.Syntax;
import com.zarbosoft.merman.syntax.style.ModelColor;
import com.zarbosoft.rendaw.common.DeadCode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.zarbosoft.merman.modules.hotkeys.Key.*;

public class JavaFXDisplay implements Display {
	public final Pane node = new Pane();
	private final javafx.scene.Group origin = new javafx.scene.Group();
	Set<JavaFXNode> dirty = new HashSet<>();

	Set<Key> modifiers = new HashSet<>();
	List<Runnable> mouseExitListeners = new ArrayList<>();
	List<Consumer<Vector>> mouseMoveListeners = new ArrayList<>();
	List<Consumer<HIDEvent>> hidEventListeners = new ArrayList<>();
	List<Consumer<String>> typingListeners = new ArrayList<>();
	List<IntListener> converseEdgeListeners = new ArrayList<>();
	List<IntListener> transverseEdgeListeners = new ArrayList<>();
	int oldConverseEdge = Integer.MAX_VALUE;
	int oldTransverseEdge = Integer.MAX_VALUE;

	public JavaFXDisplay(final Syntax syntax) {
		node.setSnapToPixel(true);
		node.setFocusTraversable(true);
		node.getChildren().add(origin);
		node.setOnMouseExited(event -> {
			ImmutableList.copyOf(mouseExitListeners).forEach(l -> l.run());
		});
		node.setOnMouseMoved(event -> {
			final Vector position = sceneToVector(syntax, event.getX(), event.getY());
			ImmutableList.copyOf(mouseMoveListeners).forEach(l -> l.accept(position));
		});
		node.setOnMousePressed(e -> {
			node.requestFocus();
			final HIDEvent event = buildHIDEvent(convertButton(e.getButton()), true);
			ImmutableList.copyOf(hidEventListeners).forEach(l -> l.accept(event));
		});
		node.setOnMouseReleased(e -> {
			node.requestFocus();
			final HIDEvent event = buildHIDEvent(convertButton(e.getButton()), false);
			ImmutableList.copyOf(hidEventListeners).forEach(l -> l.accept(event));
		});
		node.setOnScroll(e -> {
			node.requestFocus();
			final HIDEvent event = buildHIDEvent(e.getDeltaY() > 0 ? Key.MOUSE_SCROLL_DOWN : Key.MOUSE_SCROLL_UP, true);
			ImmutableList.copyOf(hidEventListeners).forEach(l -> l.accept(event));
		});
		node.setOnKeyPressed(e -> {
			final HIDEvent event = buildHIDEvent(convertButton(e.getCode()), true);
			ImmutableList.copyOf(hidEventListeners).forEach(l -> l.accept(event));
			if (e.getCode() == KeyCode.ENTER) {
				ImmutableList.copyOf(typingListeners).forEach(l -> l.accept("\n"));
			}
			e.consume();
		});
		node.setOnKeyReleased(e -> {
			final HIDEvent event = buildHIDEvent(convertButton(e.getCode()), false);
			ImmutableList.copyOf(hidEventListeners).forEach(l -> l.accept(event));
			e.consume();
		});
		node.setOnKeyTyped(e -> {
			final String text = e.getCharacter();
			ImmutableList.copyOf(typingListeners).forEach(l -> l.accept(text));
			e.consume();
		});
		final ChangeListener<Number> converseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue1 = (int) newValue.doubleValue();
			// JavaFX does something stupid where middle mouse wheel causes the node to resize by 1px
			if (Math.abs(oldConverseEdge - newValue1) < 10)
				return;
			ImmutableList.copyOf(converseEdgeListeners).forEach(l -> {
				l.changed(oldConverseEdge, newValue1);
			});
			oldConverseEdge = newValue1;
		};
		final ChangeListener<Number> transverseSizeListener = (observable, oldValue, newValue) -> {
			final int newValue1 = (int) newValue.doubleValue();
			// JavaFX does something stupid where middle mouse wheel causes the node to resize by 1px
			if (Math.abs(oldTransverseEdge - newValue1) < 10)
				return;
			ImmutableList.copyOf(transverseEdgeListeners).forEach(l -> l.changed(oldTransverseEdge, newValue1));
			oldTransverseEdge = newValue1;
		};
		switch (syntax.converseDirection) {
			case UP:
			case DOWN:
				node.heightProperty().addListener(converseSizeListener);
				node.widthProperty().addListener(transverseSizeListener);
				break;
			case LEFT:
			case RIGHT:
				node.widthProperty().addListener(converseSizeListener);
				node.heightProperty().addListener(transverseSizeListener);
				break;
		}
		final ChangeListener<Number> clipListener = new ChangeListener<>() {
			@Override
			public void changed(
					final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue
			) {
				node.setClip(new Rectangle(node.getWidth(), node.getHeight()));
			}
		};
		node.heightProperty().addListener(clipListener);
		node.widthProperty().addListener(clipListener);
		if (syntax.converseDirection == Syntax.Direction.LEFT || syntax.transverseDirection == Syntax.Direction.LEFT) {
			node.widthProperty().addListener(new ChangeListener<>() {
				@Override
				public void changed(
						final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue
				) {
					origin.setLayoutX(newValue.doubleValue());
				}
			});
		}
		if (syntax.converseDirection == Syntax.Direction.UP || syntax.transverseDirection == Syntax.Direction.UP) {
			node.heightProperty().addListener(new ChangeListener<>() {
				@Override
				public void changed(
						final ObservableValue<? extends Number> observable, final Number oldValue, final Number newValue
				) {
					origin.setLayoutY(newValue.doubleValue());
				}
			});
		}
	}

	public void addMouseExitListener(final Runnable listener) {
		mouseExitListeners.add(listener);
	}

	public void addMouseMoveListener(final Consumer<Vector> listener) {
		mouseMoveListeners.add(listener);
	}

	public void addHIDEventListener(final Consumer<HIDEvent> listener) {
		hidEventListeners.add(listener);
	}

	public void addTypingListener(final Consumer<String> listener) {
		typingListeners.add(listener);
	}

	@Override
	public void focus() {
		node.requestFocus();
	}

	@Override
	public int edge(final Context context) {
		switch (context.syntax.converseDirection) {
			case UP:
			case DOWN:
				return (int) node.getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node.getLayoutBounds().getWidth();
		}
		throw new DeadCode();
	}

	public void addConverseEdgeListener(final IntListener listener) {
		converseEdgeListeners.add(listener);
	}

	@Override
	public void removeConverseEdgeListener(final IntListener listener) {
		converseEdgeListeners.remove(listener);
	}

	@Override
	public int transverseEdge(final Context context) {
		switch (context.syntax.transverseDirection) {
			case UP:
			case DOWN:
				return (int) node.getLayoutBounds().getHeight();
			case LEFT:
			case RIGHT:
				return (int) node.getLayoutBounds().getWidth();
		}
		throw new DeadCode();
	}

	public void addTransverseEdgeListener(final IntListener listener) {
		transverseEdgeListeners.add(listener);
	}

	@Override
	public void removeTransverseEdgeListener(final IntListener listener) {
		transverseEdgeListeners.remove(listener);
	}

	@Override
	public Group group() {
		return new JavaFXGroup(this);
	}

	@Override
	public Image image() {
		return new JavaFXImage();
	}

	@Override
	public Text text() {
		return new JavaFXText();
	}

	@Override
	public Font font(final String font, final int fontSize) {
		return new JavaFXFont(font, fontSize);
	}

	@Override
	public Drawing drawing() {
		return new JavaFXDrawing();
	}

	@Override
	public Blank blank() {
		return new JavaFXBlank();
	}

	@Override
	public void add(final int index, final DisplayNode node) {
		this.origin.getChildren().add(index, ((JavaFXNode) node).node());
	}

	@Override
	public void remove(final DisplayNode node) {
		this.origin.getChildren().remove(((JavaFXNode) node).node());
	}

	@Override
	public int size() {
		return origin.getChildren().size();
	}

	@Override
	public void setBackgroundColor(final ModelColor color) {
		node.setBackground(new Background(new BackgroundFill(Helper.convert(color), null, null)));
	}

	@Override
	public void flush() {
		for (final JavaFXNode node : dirty)
			node.flush();
		dirty.clear();
	}

	public HIDEvent buildHIDEvent(final Key key, final boolean press) {
		final HIDEvent out = new HIDEvent(key, press, modifiers);
		switch (key) {
			case MOUSE_SCROLL_DOWN:
			case MOUSE_SCROLL_UP:
			case MOUSE_1:
			case MOUSE_2:
			case MOUSE_3:
				break;
			default:
				if (press)
					modifiers.add(key);
				else
					modifiers.remove(key);
		}
		return out;
	}

	public static Key convertButton(final MouseButton button) {
		switch (button) {
			case NONE:
				throw new DeadCode();
			case PRIMARY:
				return MOUSE_1;
			case MIDDLE:
				return MOUSE_3;
			case SECONDARY:
				return MOUSE_2;
		}
		throw new DeadCode();
	}

	public static Key convertButton(final KeyCode code) {
		switch (code) {
			case ENTER:
				return ENTER;

			case BACK_SPACE:
				return BACK_SPACE;

			case TAB:
				return TAB;

			case CANCEL:
				return CANCEL;

			case CLEAR:
				return CLEAR;

			case SHIFT:
				return SHIFT;

			case CONTROL:
				return CONTROL;

			case ALT:
				return ALT;

			case PAUSE:
				return PAUSE;

			case CAPS:
				return CAPS;

			case ESCAPE:
				return ESCAPE;

			case SPACE:
				return SPACE;

			case PAGE_UP:
				return PAGE_UP;

			case PAGE_DOWN:
				return PAGE_DOWN;

			case END:
				return END;

			case HOME:
				return HOME;

			case LEFT:
				return LEFT;

			case UP:
				return UP;

			case RIGHT:
				return RIGHT;

			case DOWN:
				return DOWN;

			case COMMA:
				return COMMA;

			case MINUS:
				return MINUS;

			case PERIOD:
				return PERIOD;

			case SLASH:
				return SLASH;

			case DIGIT0:
				return DIGIT0;

			case DIGIT1:
				return DIGIT1;

			case DIGIT2:
				return DIGIT2;

			case DIGIT3:
				return DIGIT3;

			case DIGIT4:
				return DIGIT4;

			case DIGIT5:
				return DIGIT5;

			case DIGIT6:
				return DIGIT6;

			case DIGIT7:
				return DIGIT7;

			case DIGIT8:
				return DIGIT8;

			case DIGIT9:
				return DIGIT9;

			case SEMICOLON:
				return SEMICOLON;

			case EQUALS:
				return EQUALS;

			case A:
				return A;

			case B:
				return B;

			case C:
				return C;

			case D:
				return D;

			case E:
				return E;

			case F:
				return F;

			case G:
				return G;

			case H:
				return H;

			case I:
				return I;

			case J:
				return J;

			case K:
				return K;

			case L:
				return L;

			case M:
				return M;

			case N:
				return N;

			case O:
				return O;

			case P:
				return P;

			case Q:
				return Q;

			case R:
				return R;

			case S:
				return S;

			case T:
				return T;

			case U:
				return U;

			case V:
				return V;

			case W:
				return W;

			case X:
				return X;

			case Y:
				return Y;

			case Z:
				return Z;

			case OPEN_BRACKET:
				return OPEN_BRACKET;

			case BACK_SLASH:
				return BACK_SLASH;

			case CLOSE_BRACKET:
				return CLOSE_BRACKET;

			case NUMPAD0:
				return NUMPAD0;

			case NUMPAD1:
				return NUMPAD1;

			case NUMPAD2:
				return NUMPAD2;

			case NUMPAD3:
				return NUMPAD3;

			case NUMPAD4:
				return NUMPAD4;

			case NUMPAD5:
				return NUMPAD5;

			case NUMPAD6:
				return NUMPAD6;

			case NUMPAD7:
				return NUMPAD7;

			case NUMPAD8:
				return NUMPAD8;

			case NUMPAD9:
				return NUMPAD9;

			case MULTIPLY:
				return MULTIPLY;

			case ADD:
				return ADD;

			case SEPARATOR:
				return SEPARATOR;

			case SUBTRACT:
				return SUBTRACT;

			case DECIMAL:
				return DECIMAL;

			case DIVIDE:
				return DIVIDE;

			case DELETE:
				return DELETE;

			case NUM_LOCK:
				return NUM_LOCK;

			case SCROLL_LOCK:
				return SCROLL_LOCK;

			case F1:
				return F1;

			case F2:
				return F2;

			case F3:
				return F3;

			case F4:
				return F4;

			case F5:
				return F5;

			case F6:
				return F6;

			case F7:
				return F7;

			case F8:
				return F8;

			case F9:
				return F9;

			case F10:
				return F10;

			case F11:
				return F11;

			case F12:
				return F12;

			case F13:
				return F13;

			case F14:
				return F14;

			case F15:
				return F15;

			case F16:
				return F16;

			case F17:
				return F17;

			case F18:
				return F18;

			case F19:
				return F19;

			case F20:
				return F20;

			case F21:
				return F21;

			case F22:
				return F22;

			case F23:
				return F23;

			case F24:
				return F24;

			case PRINTSCREEN:
				return PRINTSCREEN;

			case INSERT:
				return INSERT;

			case HELP:
				return HELP;

			case META:
				return META;

			case BACK_QUOTE:
				return BACK_QUOTE;

			case QUOTE:
				return QUOTE;

			case KP_UP:
				return KP_UP;

			case KP_DOWN:
				return KP_DOWN;

			case KP_LEFT:
				return KP_LEFT;

			case KP_RIGHT:
				return KP_RIGHT;

			case DEAD_GRAVE:
				return DEAD_GRAVE;

			case DEAD_ACUTE:
				return DEAD_ACUTE;

			case DEAD_CIRCUMFLEX:
				return DEAD_CIRCUMFLEX;

			case DEAD_TILDE:
				return DEAD_TILDE;

			case DEAD_MACRON:
				return DEAD_MACRON;

			case DEAD_BREVE:
				return DEAD_BREVE;

			case DEAD_ABOVEDOT:
				return DEAD_ABOVEDOT;

			case DEAD_DIAERESIS:
				return DEAD_DIAERESIS;

			case DEAD_ABOVERING:
				return DEAD_ABOVERING;

			case DEAD_DOUBLEACUTE:
				return DEAD_DOUBLEACUTE;

			case DEAD_CARON:
				return DEAD_CARON;

			case DEAD_CEDILLA:
				return DEAD_CEDILLA;

			case DEAD_OGONEK:
				return DEAD_OGONEK;

			case DEAD_IOTA:
				return DEAD_IOTA;

			case DEAD_VOICED_SOUND:
				return DEAD_VOICED_SOUND;

			case DEAD_SEMIVOICED_SOUND:
				return DEAD_SEMIVOICED_SOUND;

			case AMPERSAND:
				return AMPERSAND;

			case ASTERISK:
				return ASTERISK;

			case QUOTEDBL:
				return QUOTEDBL;

			case LESS:
				return LESS;

			case GREATER:
				return GREATER;

			case BRACELEFT:
				return BRACELEFT;

			case BRACERIGHT:
				return BRACERIGHT;

			case AT:
				return AT;

			case COLON:
				return COLON;

			case CIRCUMFLEX:
				return CIRCUMFLEX;

			case DOLLAR:
				return DOLLAR;

			case EURO_SIGN:
				return EURO_SIGN;

			case EXCLAMATION_MARK:
				return EXCLAMATION_MARK;

			case INVERTED_EXCLAMATION_MARK:
				return INVERTED_EXCLAMATION_MARK;

			case LEFT_PARENTHESIS:
				return LEFT_PARENTHESIS;

			case NUMBER_SIGN:
				return NUMBER_SIGN;

			case PLUS:
				return PLUS;

			case RIGHT_PARENTHESIS:
				return RIGHT_PARENTHESIS;

			case UNDERSCORE:
				return UNDERSCORE;

			case WINDOWS:
				return WINDOWS;

			case CONTEXT_MENU:
				return CONTEXT_MENU;

			case FINAL:
				return FINAL;

			case CONVERT:
				return CONVERT;

			case NONCONVERT:
				return NONCONVERT;

			case ACCEPT:
				return ACCEPT;

			case MODECHANGE:
				return MODECHANGE;

			case KANA:
				return KANA;

			case KANJI:
				return KANJI;

			case ALPHANUMERIC:
				return ALPHANUMERIC;

			case KATAKANA:
				return KATAKANA;

			case HIRAGANA:
				return HIRAGANA;

			case FULL_WIDTH:
				return FULL_WIDTH;

			case HALF_WIDTH:
				return HALF_WIDTH;

			case ROMAN_CHARACTERS:
				return ROMAN_CHARACTERS;

			case ALL_CANDIDATES:
				return ALL_CANDIDATES;

			case PREVIOUS_CANDIDATE:
				return PREVIOUS_CANDIDATE;

			case CODE_INPUT:
				return CODE_INPUT;

			case JAPANESE_KATAKANA:
				return JAPANESE_KATAKANA;

			case JAPANESE_HIRAGANA:
				return JAPANESE_HIRAGANA;

			case JAPANESE_ROMAN:
				return JAPANESE_ROMAN;

			case KANA_LOCK:
				return KANA_LOCK;

			case INPUT_METHOD_ON_OFF:
				return INPUT_METHOD_ON_OFF;

			case CUT:
				return CUT;

			case COPY:
				return COPY;

			case PASTE:
				return PASTE;

			case UNDO:
				return UNDO;

			case AGAIN:
				return AGAIN;

			case FIND:
				return FIND;

			case PROPS:
				return PROPS;

			case STOP:
				return STOP;

			case COMPOSE:
				return COMPOSE;

			case ALT_GRAPH:
				return ALT_GRAPH;

			case BEGIN:
				return BEGIN;

			case UNDEFINED:
				return UNDEFINED;

			case SOFTKEY_0:
				return SOFTKEY_0;

			case SOFTKEY_1:
				return SOFTKEY_1;

			case SOFTKEY_2:
				return SOFTKEY_2;

			case SOFTKEY_3:
				return SOFTKEY_3;

			case SOFTKEY_4:
				return SOFTKEY_4;

			case SOFTKEY_5:
				return SOFTKEY_5;

			case SOFTKEY_6:
				return SOFTKEY_6;

			case SOFTKEY_7:
				return SOFTKEY_7;

			case SOFTKEY_8:
				return SOFTKEY_8;

			case SOFTKEY_9:
				return SOFTKEY_9;

			case GAME_A:
				return GAME_A;

			case GAME_B:
				return GAME_B;

			case GAME_C:
				return GAME_C;

			case GAME_D:
				return GAME_D;

			case STAR:
				return STAR;

			case POUND:
				return POUND;

			case POWER:
				return POWER;

			case INFO:
				return INFO;

			case COLORED_KEY_0:
				return COLORED_KEY_0;

			case COLORED_KEY_1:
				return COLORED_KEY_1;

			case COLORED_KEY_2:
				return COLORED_KEY_2;

			case COLORED_KEY_3:
				return COLORED_KEY_3;

			case EJECT_TOGGLE:
				return EJECT_TOGGLE;

			case PLAY:
				return PLAY;

			case RECORD:
				return RECORD;

			case FAST_FWD:
				return FAST_FWD;

			case REWIND:
				return REWIND;

			case TRACK_PREV:
				return TRACK_PREV;

			case TRACK_NEXT:
				return TRACK_NEXT;

			case CHANNEL_UP:
				return CHANNEL_UP;

			case CHANNEL_DOWN:
				return CHANNEL_DOWN;

			case VOLUME_UP:
				return VOLUME_UP;

			case VOLUME_DOWN:
				return VOLUME_DOWN;

			case MUTE:
				return MUTE;

			case COMMAND:
				return COMMAND;

			case SHORTCUT:
				return SHORTCUT;

		}
		throw new DeadCode();
	}

	public Vector sceneToVector(
			final Syntax syntax, final double x, final double y
	) {
		int converse = 0;
		int transverse = 0;
		final Bounds bounds = node.getLayoutBounds();
		switch (syntax.converseDirection) {
			case UP:
				converse = (int) (bounds.getHeight() - y);
				break;
			case DOWN:
				converse = (int) y;
				break;
			case LEFT:
				converse = (int) (bounds.getWidth() - x);
				break;
			case RIGHT:
				converse = (int) x;
				break;
		}
		switch (syntax.transverseDirection) {
			case UP:
				transverse = (int) (bounds.getHeight() - x);
				break;
			case DOWN:
				transverse = (int) y;
				break;
			case LEFT:
				transverse = (int) (bounds.getWidth() - x);
				break;
			case RIGHT:
				transverse = (int) x;
				break;
		}
		return new Vector(converse, transverse);
	}
}
