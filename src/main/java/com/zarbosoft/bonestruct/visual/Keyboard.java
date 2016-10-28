package com.zarbosoft.bonestruct.visual;

import com.zarbosoft.luxemj.Luxem;
import com.zarbosoft.pidgoon.internal.Helper;
import com.zarbosoft.pidgoon.internal.Node;
import com.zarbosoft.pidgoon.nodes.Sequence;
import javafx.scene.input.KeyCode;

public class Keyboard {

	public static Node ruleFromString(final String name) {
		final Sequence out = new Sequence();
		for (int i = 0; i < name.length(); ++i) {
			try {
				out.add(new com.zarbosoft.pidgoon.events.Terminal(new Keyboard.Event(fromChar(name.charAt(i)),
						false,
						false,
						false
				)));
			} catch (final IllegalArgumentException e) {
				continue;
			}
		}
		return out;
	}

	private static Key fromChar(final char at) {
		switch (at) {
			case 'a':
				return Key.A;
			case 'b':
				return Key.B;
			case 'c':
				return Key.C;
			case 'd':
				return Key.D;
			case 'e':
				return Key.E;
			case 'f':
				return Key.F;
			case 'g':
				return Key.G;
			case 'h':
				return Key.H;
			case 'i':
				return Key.I;
			case 'j':
				return Key.J;
			case 'k':
				return Key.K;
			case 'l':
				return Key.L;
			case 'm':
				return Key.M;
			case 'n':
				return Key.N;
			case 'o':
				return Key.O;
			case 'p':
				return Key.P;
			case 'q':
				return Key.Q;
			case 'r':
				return Key.R;
			case 's':
				return Key.S;
			case 't':
				return Key.T;
			case 'u':
				return Key.U;
			case 'v':
				return Key.V;
			case 'w':
				return Key.W;
			case 'x':
				return Key.X;
			case 'y':
				return Key.Y;
			case 'z':
				return Key.Z;
			case '-':
				return Key.MINUS;
			case ':':
				return Key.COLON;
		}
		throw new IllegalArgumentException();
	}

	@Luxem.Configuration(name = "key")
	public static class Terminal extends com.zarbosoft.luxemj.com.zarbosoft.luxemj.grammar.Terminal {
		@Luxem.Configuration
		public Key key;
		@Luxem.Configuration(optional = true)
		public boolean control = false;
		@Luxem.Configuration(optional = true)
		public boolean shift = false;
		@Luxem.Configuration(optional = true)
		public boolean alt = false;

		@Override
		public com.zarbosoft.pidgoon.events.Event getEvent() {
			return new Event(key, control, shift, alt);
		}
	}

	public static class Event implements com.zarbosoft.pidgoon.events.Event {
		public Event(final Key key, final boolean control, final boolean shift, final boolean alt) {
			this.key = key;
			this.control = control;
			this.shift = shift;
			this.alt = alt;
		}

		Key key;
		boolean control;
		boolean shift;
		boolean alt;

		@Override
		public boolean matches(final com.zarbosoft.pidgoon.events.Event event) {
			final Event keyEvent = (Event) event;
			if (key != keyEvent.key)
				return false;
			if (control != keyEvent.control)
				return false;
			if (shift != keyEvent.shift)
				return false;
			if (alt != keyEvent.alt)
				return false;
			return true;
		}

		@Override
		public String toString() {
			final StringBuilder out = new StringBuilder();
			if (control)
				out.append("ctrl+");
			if (alt)
				out.append("alt+");
			if (shift)
				out.append("ctrl+");
			final Luxem.Configuration annotation =
					Helper.uncheck(() -> Key.class.getField(key.name()).getAnnotation(Luxem.Configuration.class));
			out.append(annotation.name());
			return out.toString();
		}
	}

	@Luxem.Configuration
	public enum Key {
		@Luxem.Configuration(name = "enter")
		ENTER,

		@Luxem.Configuration(name = "back-space")
		BACK_SPACE,

		@Luxem.Configuration(name = "tab")
		TAB,

		@Luxem.Configuration(name = "cancel")
		CANCEL,

		@Luxem.Configuration(name = "clear")
		CLEAR,

		@Luxem.Configuration(name = "shift")
		SHIFT,

		@Luxem.Configuration(name = "control")
		CONTROL,

		@Luxem.Configuration(name = "alt")
		ALT,

		@Luxem.Configuration(name = "pause")
		PAUSE,

		@Luxem.Configuration(name = "caps")
		CAPS,

		@Luxem.Configuration(name = "escape")
		ESCAPE,

		@Luxem.Configuration(name = "space")
		SPACE,

		@Luxem.Configuration(name = "page-up")
		PAGE_UP,

		@Luxem.Configuration(name = "page-down")
		PAGE_DOWN,

		@Luxem.Configuration(name = "end")
		END,

		@Luxem.Configuration(name = "home")
		HOME,

		@Luxem.Configuration(name = "left")
		LEFT,

		@Luxem.Configuration(name = "up")
		UP,

		@Luxem.Configuration(name = "right")
		RIGHT,

		@Luxem.Configuration(name = "down")
		DOWN,

		@Luxem.Configuration(name = "comma")
		COMMA,

		@Luxem.Configuration(name = "minus")
		MINUS,

		@Luxem.Configuration(name = "period")
		PERIOD,

		@Luxem.Configuration(name = "slash")
		SLASH,

		@Luxem.Configuration(name = "digit0")
		DIGIT0,

		@Luxem.Configuration(name = "digit1")
		DIGIT1,

		@Luxem.Configuration(name = "digit2")
		DIGIT2,

		@Luxem.Configuration(name = "digit3")
		DIGIT3,

		@Luxem.Configuration(name = "digit4")
		DIGIT4,

		@Luxem.Configuration(name = "digit5")
		DIGIT5,

		@Luxem.Configuration(name = "digit6")
		DIGIT6,

		@Luxem.Configuration(name = "digit7")
		DIGIT7,

		@Luxem.Configuration(name = "digit8")
		DIGIT8,

		@Luxem.Configuration(name = "digit9")
		DIGIT9,

		@Luxem.Configuration(name = "semicolon")
		SEMICOLON,

		@Luxem.Configuration(name = "equals")
		EQUALS,

		@Luxem.Configuration(name = "a")
		A,

		@Luxem.Configuration(name = "b")
		B,

		@Luxem.Configuration(name = "c")
		C,

		@Luxem.Configuration(name = "d")
		D,

		@Luxem.Configuration(name = "e")
		E,

		@Luxem.Configuration(name = "f")
		F,

		@Luxem.Configuration(name = "g")
		G,

		@Luxem.Configuration(name = "h")
		H,

		@Luxem.Configuration(name = "i")
		I,

		@Luxem.Configuration(name = "j")
		J,

		@Luxem.Configuration(name = "k")
		K,

		@Luxem.Configuration(name = "l")
		L,

		@Luxem.Configuration(name = "m")
		M,

		@Luxem.Configuration(name = "n")
		N,

		@Luxem.Configuration(name = "o")
		O,

		@Luxem.Configuration(name = "p")
		P,

		@Luxem.Configuration(name = "q")
		Q,

		@Luxem.Configuration(name = "r")
		R,

		@Luxem.Configuration(name = "s")
		S,

		@Luxem.Configuration(name = "t")
		T,

		@Luxem.Configuration(name = "u")
		U,

		@Luxem.Configuration(name = "v")
		V,

		@Luxem.Configuration(name = "w")
		W,

		@Luxem.Configuration(name = "x")
		X,

		@Luxem.Configuration(name = "y")
		Y,

		@Luxem.Configuration(name = "z")
		Z,

		@Luxem.Configuration(name = "open-bracket")
		OPEN_BRACKET,

		@Luxem.Configuration(name = "back-slash")
		BACK_SLASH,

		@Luxem.Configuration(name = "close-bracket")
		CLOSE_BRACKET,

		@Luxem.Configuration(name = "numpad0")
		NUMPAD0,

		@Luxem.Configuration(name = "numpad1")
		NUMPAD1,

		@Luxem.Configuration(name = "numpad2")
		NUMPAD2,

		@Luxem.Configuration(name = "numpad3")
		NUMPAD3,

		@Luxem.Configuration(name = "numpad4")
		NUMPAD4,

		@Luxem.Configuration(name = "numpad5")
		NUMPAD5,

		@Luxem.Configuration(name = "numpad6")
		NUMPAD6,

		@Luxem.Configuration(name = "numpad7")
		NUMPAD7,

		@Luxem.Configuration(name = "numpad8")
		NUMPAD8,

		@Luxem.Configuration(name = "numpad9")
		NUMPAD9,

		@Luxem.Configuration(name = "multiply")
		MULTIPLY,

		@Luxem.Configuration(name = "add")
		ADD,

		@Luxem.Configuration(name = "separator")
		SEPARATOR,

		@Luxem.Configuration(name = "subtract")
		SUBTRACT,

		@Luxem.Configuration(name = "decimal")
		DECIMAL,

		@Luxem.Configuration(name = "divide")
		DIVIDE,

		@Luxem.Configuration(name = "delete")
		DELETE,

		@Luxem.Configuration(name = "num-lock")
		NUM_LOCK,

		@Luxem.Configuration(name = "scroll-lock")
		SCROLL_LOCK,

		@Luxem.Configuration(name = "f1")
		F1,

		@Luxem.Configuration(name = "f2")
		F2,

		@Luxem.Configuration(name = "f3")
		F3,

		@Luxem.Configuration(name = "f4")
		F4,

		@Luxem.Configuration(name = "f5")
		F5,

		@Luxem.Configuration(name = "f6")
		F6,

		@Luxem.Configuration(name = "f7")
		F7,

		@Luxem.Configuration(name = "f8")
		F8,

		@Luxem.Configuration(name = "f9")
		F9,

		@Luxem.Configuration(name = "f10")
		F10,

		@Luxem.Configuration(name = "f11")
		F11,

		@Luxem.Configuration(name = "f12")
		F12,

		@Luxem.Configuration(name = "f13")
		F13,

		@Luxem.Configuration(name = "f14")
		F14,

		@Luxem.Configuration(name = "f15")
		F15,

		@Luxem.Configuration(name = "f16")
		F16,

		@Luxem.Configuration(name = "f17")
		F17,

		@Luxem.Configuration(name = "f18")
		F18,

		@Luxem.Configuration(name = "f19")
		F19,

		@Luxem.Configuration(name = "f20")
		F20,

		@Luxem.Configuration(name = "f21")
		F21,

		@Luxem.Configuration(name = "f22")
		F22,

		@Luxem.Configuration(name = "f23")
		F23,

		@Luxem.Configuration(name = "f24")
		F24,

		@Luxem.Configuration(name = "printscreen")
		PRINTSCREEN,

		@Luxem.Configuration(name = "insert")
		INSERT,

		@Luxem.Configuration(name = "help")
		HELP,

		@Luxem.Configuration(name = "meta")
		META,

		@Luxem.Configuration(name = "back-quote")
		BACK_QUOTE,

		@Luxem.Configuration(name = "quote")
		QUOTE,

		@Luxem.Configuration(name = "kp-up")
		KP_UP,

		@Luxem.Configuration(name = "kp-down")
		KP_DOWN,

		@Luxem.Configuration(name = "kp-left")
		KP_LEFT,

		@Luxem.Configuration(name = "kp-right")
		KP_RIGHT,

		@Luxem.Configuration(name = "dead-grave")
		DEAD_GRAVE,

		@Luxem.Configuration(name = "dead-acute")
		DEAD_ACUTE,

		@Luxem.Configuration(name = "dead-circumflex")
		DEAD_CIRCUMFLEX,

		@Luxem.Configuration(name = "dead-tilde")
		DEAD_TILDE,

		@Luxem.Configuration(name = "dead-macron")
		DEAD_MACRON,

		@Luxem.Configuration(name = "dead-breve")
		DEAD_BREVE,

		@Luxem.Configuration(name = "dead-abovedot")
		DEAD_ABOVEDOT,

		@Luxem.Configuration(name = "dead-diaeresis")
		DEAD_DIAERESIS,

		@Luxem.Configuration(name = "dead-abovering")
		DEAD_ABOVERING,

		@Luxem.Configuration(name = "dead-doubleacute")
		DEAD_DOUBLEACUTE,

		@Luxem.Configuration(name = "dead-caron")
		DEAD_CARON,

		@Luxem.Configuration(name = "dead-cedilla")
		DEAD_CEDILLA,

		@Luxem.Configuration(name = "dead-ogonek")
		DEAD_OGONEK,

		@Luxem.Configuration(name = "dead-iota")
		DEAD_IOTA,

		@Luxem.Configuration(name = "dead_voiced-sound")
		DEAD_VOICED_SOUND,

		@Luxem.Configuration(name = "dead_semivoiced-sound")
		DEAD_SEMIVOICED_SOUND,

		@Luxem.Configuration(name = "ampersand")
		AMPERSAND,

		@Luxem.Configuration(name = "asterisk")
		ASTERISK,

		@Luxem.Configuration(name = "quotedbl")
		QUOTEDBL,

		@Luxem.Configuration(name = "less")
		LESS,

		@Luxem.Configuration(name = "greater")
		GREATER,

		@Luxem.Configuration(name = "braceleft")
		BRACELEFT,

		@Luxem.Configuration(name = "braceright")
		BRACERIGHT,

		@Luxem.Configuration(name = "at")
		AT,

		@Luxem.Configuration(name = "colon")
		COLON,

		@Luxem.Configuration(name = "circumflex")
		CIRCUMFLEX,

		@Luxem.Configuration(name = "dollar")
		DOLLAR,

		@Luxem.Configuration(name = "euro-sign")
		EURO_SIGN,

		@Luxem.Configuration(name = "exclamation-mark")
		EXCLAMATION_MARK,

		@Luxem.Configuration(name = "inverted_exclamation-mark")
		INVERTED_EXCLAMATION_MARK,

		@Luxem.Configuration(name = "left-parenthesis")
		LEFT_PARENTHESIS,

		@Luxem.Configuration(name = "number-sign")
		NUMBER_SIGN,

		@Luxem.Configuration(name = "plus")
		PLUS,

		@Luxem.Configuration(name = "right-parenthesis")
		RIGHT_PARENTHESIS,

		@Luxem.Configuration(name = "underscore")
		UNDERSCORE,

		@Luxem.Configuration(name = "windows")
		WINDOWS,

		@Luxem.Configuration(name = "context-menu")
		CONTEXT_MENU,

		@Luxem.Configuration(name = "final")
		FINAL,

		@Luxem.Configuration(name = "convert")
		CONVERT,

		@Luxem.Configuration(name = "nonconvert")
		NONCONVERT,

		@Luxem.Configuration(name = "accept")
		ACCEPT,

		@Luxem.Configuration(name = "modechange")
		MODECHANGE,

		@Luxem.Configuration(name = "kana")
		KANA,

		@Luxem.Configuration(name = "kanji")
		KANJI,

		@Luxem.Configuration(name = "alphanumeric")
		ALPHANUMERIC,

		@Luxem.Configuration(name = "katakana")
		KATAKANA,

		@Luxem.Configuration(name = "hiragana")
		HIRAGANA,

		@Luxem.Configuration(name = "full-width")
		FULL_WIDTH,

		@Luxem.Configuration(name = "half-width")
		HALF_WIDTH,

		@Luxem.Configuration(name = "roman-characters")
		ROMAN_CHARACTERS,

		@Luxem.Configuration(name = "all-candidates")
		ALL_CANDIDATES,

		@Luxem.Configuration(name = "previous-candidate")
		PREVIOUS_CANDIDATE,

		@Luxem.Configuration(name = "code-input")
		CODE_INPUT,

		@Luxem.Configuration(name = "japanese-katakana")
		JAPANESE_KATAKANA,

		@Luxem.Configuration(name = "japanese-hiragana")
		JAPANESE_HIRAGANA,

		@Luxem.Configuration(name = "japanese-roman")
		JAPANESE_ROMAN,

		@Luxem.Configuration(name = "kana-lock")
		KANA_LOCK,

		@Luxem.Configuration(name = "input_method_on-off")
		INPUT_METHOD_ON_OFF,

		@Luxem.Configuration(name = "cut")
		CUT,

		@Luxem.Configuration(name = "copy")
		COPY,

		@Luxem.Configuration(name = "paste")
		PASTE,

		@Luxem.Configuration(name = "undo")
		UNDO,

		@Luxem.Configuration(name = "again")
		AGAIN,

		@Luxem.Configuration(name = "find")
		FIND,

		@Luxem.Configuration(name = "props")
		PROPS,

		@Luxem.Configuration(name = "stop")
		STOP,

		@Luxem.Configuration(name = "compose")
		COMPOSE,

		@Luxem.Configuration(name = "alt-graph")
		ALT_GRAPH,

		@Luxem.Configuration(name = "begin")
		BEGIN,

		@Luxem.Configuration(name = "undefined")
		UNDEFINED,

		@Luxem.Configuration(name = "softkey-0")
		SOFTKEY_0,

		@Luxem.Configuration(name = "softkey-1")
		SOFTKEY_1,

		@Luxem.Configuration(name = "softkey-2")
		SOFTKEY_2,

		@Luxem.Configuration(name = "softkey-3")
		SOFTKEY_3,

		@Luxem.Configuration(name = "softkey-4")
		SOFTKEY_4,

		@Luxem.Configuration(name = "softkey-5")
		SOFTKEY_5,

		@Luxem.Configuration(name = "softkey-6")
		SOFTKEY_6,

		@Luxem.Configuration(name = "softkey-7")
		SOFTKEY_7,

		@Luxem.Configuration(name = "softkey-8")
		SOFTKEY_8,

		@Luxem.Configuration(name = "softkey-9")
		SOFTKEY_9,

		@Luxem.Configuration(name = "game-a")
		GAME_A,

		@Luxem.Configuration(name = "game-b")
		GAME_B,

		@Luxem.Configuration(name = "game-c")
		GAME_C,

		@Luxem.Configuration(name = "game-d")
		GAME_D,

		@Luxem.Configuration(name = "star")
		STAR,

		@Luxem.Configuration(name = "pound")
		POUND,

		@Luxem.Configuration(name = "power")
		POWER,

		@Luxem.Configuration(name = "info")
		INFO,

		@Luxem.Configuration(name = "colored_key-0")
		COLORED_KEY_0,

		@Luxem.Configuration(name = "colored_key-1")
		COLORED_KEY_1,

		@Luxem.Configuration(name = "colored_key-2")
		COLORED_KEY_2,

		@Luxem.Configuration(name = "colored_key-3")
		COLORED_KEY_3,

		@Luxem.Configuration(name = "eject-toggle")
		EJECT_TOGGLE,

		@Luxem.Configuration(name = "play")
		PLAY,

		@Luxem.Configuration(name = "record")
		RECORD,

		@Luxem.Configuration(name = "fast-fwd")
		FAST_FWD,

		@Luxem.Configuration(name = "rewind")
		REWIND,

		@Luxem.Configuration(name = "track-prev")
		TRACK_PREV,

		@Luxem.Configuration(name = "track-next")
		TRACK_NEXT,

		@Luxem.Configuration(name = "channel-up")
		CHANNEL_UP,

		@Luxem.Configuration(name = "channel-down")
		CHANNEL_DOWN,

		@Luxem.Configuration(name = "volume-up")
		VOLUME_UP,

		@Luxem.Configuration(name = "volume-down")
		VOLUME_DOWN,

		@Luxem.Configuration(name = "mute")
		MUTE,

		@Luxem.Configuration(name = "command")
		COMMAND,

		@Luxem.Configuration(name = "shortcut")
		SHORTCUT,

	}

	public static Key fromJFX(final KeyCode code) {
		switch (code) {
			case ENTER:
				return Keyboard.Key.ENTER;

			case BACK_SPACE:
				return Keyboard.Key.BACK_SPACE;

			case TAB:
				return Keyboard.Key.TAB;

			case CANCEL:
				return Keyboard.Key.CANCEL;

			case CLEAR:
				return Keyboard.Key.CLEAR;

			case SHIFT:
				return Keyboard.Key.SHIFT;

			case CONTROL:
				return Keyboard.Key.CONTROL;

			case ALT:
				return Keyboard.Key.ALT;

			case PAUSE:
				return Keyboard.Key.PAUSE;

			case CAPS:
				return Keyboard.Key.CAPS;

			case ESCAPE:
				return Keyboard.Key.ESCAPE;

			case SPACE:
				return Keyboard.Key.SPACE;

			case PAGE_UP:
				return Keyboard.Key.PAGE_UP;

			case PAGE_DOWN:
				return Keyboard.Key.PAGE_DOWN;

			case END:
				return Keyboard.Key.END;

			case HOME:
				return Keyboard.Key.HOME;

			case LEFT:
				return Keyboard.Key.LEFT;

			case UP:
				return Keyboard.Key.UP;

			case RIGHT:
				return Keyboard.Key.RIGHT;

			case DOWN:
				return Keyboard.Key.DOWN;

			case COMMA:
				return Keyboard.Key.COMMA;

			case MINUS:
				return Keyboard.Key.MINUS;

			case PERIOD:
				return Keyboard.Key.PERIOD;

			case SLASH:
				return Keyboard.Key.SLASH;

			case DIGIT0:
				return Keyboard.Key.DIGIT0;

			case DIGIT1:
				return Keyboard.Key.DIGIT1;

			case DIGIT2:
				return Keyboard.Key.DIGIT2;

			case DIGIT3:
				return Keyboard.Key.DIGIT3;

			case DIGIT4:
				return Keyboard.Key.DIGIT4;

			case DIGIT5:
				return Keyboard.Key.DIGIT5;

			case DIGIT6:
				return Keyboard.Key.DIGIT6;

			case DIGIT7:
				return Keyboard.Key.DIGIT7;

			case DIGIT8:
				return Keyboard.Key.DIGIT8;

			case DIGIT9:
				return Keyboard.Key.DIGIT9;

			case SEMICOLON:
				return Keyboard.Key.SEMICOLON;

			case EQUALS:
				return Keyboard.Key.EQUALS;

			case A:
				return Keyboard.Key.A;

			case B:
				return Keyboard.Key.B;

			case C:
				return Keyboard.Key.C;

			case D:
				return Keyboard.Key.D;

			case E:
				return Keyboard.Key.E;

			case F:
				return Keyboard.Key.F;

			case G:
				return Keyboard.Key.G;

			case H:
				return Keyboard.Key.H;

			case I:
				return Keyboard.Key.I;

			case J:
				return Keyboard.Key.J;

			case K:
				return Keyboard.Key.K;

			case L:
				return Keyboard.Key.L;

			case M:
				return Keyboard.Key.M;

			case N:
				return Keyboard.Key.N;

			case O:
				return Keyboard.Key.O;

			case P:
				return Keyboard.Key.P;

			case Q:
				return Keyboard.Key.Q;

			case R:
				return Keyboard.Key.R;

			case S:
				return Keyboard.Key.S;

			case T:
				return Keyboard.Key.T;

			case U:
				return Keyboard.Key.U;

			case V:
				return Keyboard.Key.V;

			case W:
				return Keyboard.Key.W;

			case X:
				return Keyboard.Key.X;

			case Y:
				return Keyboard.Key.Y;

			case Z:
				return Keyboard.Key.Z;

			case OPEN_BRACKET:
				return Keyboard.Key.OPEN_BRACKET;

			case BACK_SLASH:
				return Keyboard.Key.BACK_SLASH;

			case CLOSE_BRACKET:
				return Keyboard.Key.CLOSE_BRACKET;

			case NUMPAD0:
				return Keyboard.Key.NUMPAD0;

			case NUMPAD1:
				return Keyboard.Key.NUMPAD1;

			case NUMPAD2:
				return Keyboard.Key.NUMPAD2;

			case NUMPAD3:
				return Keyboard.Key.NUMPAD3;

			case NUMPAD4:
				return Keyboard.Key.NUMPAD4;

			case NUMPAD5:
				return Keyboard.Key.NUMPAD5;

			case NUMPAD6:
				return Keyboard.Key.NUMPAD6;

			case NUMPAD7:
				return Keyboard.Key.NUMPAD7;

			case NUMPAD8:
				return Keyboard.Key.NUMPAD8;

			case NUMPAD9:
				return Keyboard.Key.NUMPAD9;

			case MULTIPLY:
				return Keyboard.Key.MULTIPLY;

			case ADD:
				return Keyboard.Key.ADD;

			case SEPARATOR:
				return Keyboard.Key.SEPARATOR;

			case SUBTRACT:
				return Keyboard.Key.SUBTRACT;

			case DECIMAL:
				return Keyboard.Key.DECIMAL;

			case DIVIDE:
				return Keyboard.Key.DIVIDE;

			case DELETE:
				return Keyboard.Key.DELETE;

			case NUM_LOCK:
				return Keyboard.Key.NUM_LOCK;

			case SCROLL_LOCK:
				return Keyboard.Key.SCROLL_LOCK;

			case F1:
				return Keyboard.Key.F1;

			case F2:
				return Keyboard.Key.F2;

			case F3:
				return Keyboard.Key.F3;

			case F4:
				return Keyboard.Key.F4;

			case F5:
				return Keyboard.Key.F5;

			case F6:
				return Keyboard.Key.F6;

			case F7:
				return Keyboard.Key.F7;

			case F8:
				return Keyboard.Key.F8;

			case F9:
				return Keyboard.Key.F9;

			case F10:
				return Keyboard.Key.F10;

			case F11:
				return Keyboard.Key.F11;

			case F12:
				return Keyboard.Key.F12;

			case F13:
				return Keyboard.Key.F13;

			case F14:
				return Keyboard.Key.F14;

			case F15:
				return Keyboard.Key.F15;

			case F16:
				return Keyboard.Key.F16;

			case F17:
				return Keyboard.Key.F17;

			case F18:
				return Keyboard.Key.F18;

			case F19:
				return Keyboard.Key.F19;

			case F20:
				return Keyboard.Key.F20;

			case F21:
				return Keyboard.Key.F21;

			case F22:
				return Keyboard.Key.F22;

			case F23:
				return Keyboard.Key.F23;

			case F24:
				return Keyboard.Key.F24;

			case PRINTSCREEN:
				return Keyboard.Key.PRINTSCREEN;

			case INSERT:
				return Keyboard.Key.INSERT;

			case HELP:
				return Keyboard.Key.HELP;

			case META:
				return Keyboard.Key.META;

			case BACK_QUOTE:
				return Keyboard.Key.BACK_QUOTE;

			case QUOTE:
				return Keyboard.Key.QUOTE;

			case KP_UP:
				return Keyboard.Key.KP_UP;

			case KP_DOWN:
				return Keyboard.Key.KP_DOWN;

			case KP_LEFT:
				return Keyboard.Key.KP_LEFT;

			case KP_RIGHT:
				return Keyboard.Key.KP_RIGHT;

			case DEAD_GRAVE:
				return Keyboard.Key.DEAD_GRAVE;

			case DEAD_ACUTE:
				return Keyboard.Key.DEAD_ACUTE;

			case DEAD_CIRCUMFLEX:
				return Keyboard.Key.DEAD_CIRCUMFLEX;

			case DEAD_TILDE:
				return Keyboard.Key.DEAD_TILDE;

			case DEAD_MACRON:
				return Keyboard.Key.DEAD_MACRON;

			case DEAD_BREVE:
				return Keyboard.Key.DEAD_BREVE;

			case DEAD_ABOVEDOT:
				return Keyboard.Key.DEAD_ABOVEDOT;

			case DEAD_DIAERESIS:
				return Keyboard.Key.DEAD_DIAERESIS;

			case DEAD_ABOVERING:
				return Keyboard.Key.DEAD_ABOVERING;

			case DEAD_DOUBLEACUTE:
				return Keyboard.Key.DEAD_DOUBLEACUTE;

			case DEAD_CARON:
				return Keyboard.Key.DEAD_CARON;

			case DEAD_CEDILLA:
				return Keyboard.Key.DEAD_CEDILLA;

			case DEAD_OGONEK:
				return Keyboard.Key.DEAD_OGONEK;

			case DEAD_IOTA:
				return Keyboard.Key.DEAD_IOTA;

			case DEAD_VOICED_SOUND:
				return Keyboard.Key.DEAD_VOICED_SOUND;

			case DEAD_SEMIVOICED_SOUND:
				return Keyboard.Key.DEAD_SEMIVOICED_SOUND;

			case AMPERSAND:
				return Keyboard.Key.AMPERSAND;

			case ASTERISK:
				return Keyboard.Key.ASTERISK;

			case QUOTEDBL:
				return Keyboard.Key.QUOTEDBL;

			case LESS:
				return Keyboard.Key.LESS;

			case GREATER:
				return Keyboard.Key.GREATER;

			case BRACELEFT:
				return Keyboard.Key.BRACELEFT;

			case BRACERIGHT:
				return Keyboard.Key.BRACERIGHT;

			case AT:
				return Keyboard.Key.AT;

			case COLON:
				return Keyboard.Key.COLON;

			case CIRCUMFLEX:
				return Keyboard.Key.CIRCUMFLEX;

			case DOLLAR:
				return Keyboard.Key.DOLLAR;

			case EURO_SIGN:
				return Keyboard.Key.EURO_SIGN;

			case EXCLAMATION_MARK:
				return Keyboard.Key.EXCLAMATION_MARK;

			case INVERTED_EXCLAMATION_MARK:
				return Keyboard.Key.INVERTED_EXCLAMATION_MARK;

			case LEFT_PARENTHESIS:
				return Keyboard.Key.LEFT_PARENTHESIS;

			case NUMBER_SIGN:
				return Keyboard.Key.NUMBER_SIGN;

			case PLUS:
				return Keyboard.Key.PLUS;

			case RIGHT_PARENTHESIS:
				return Keyboard.Key.RIGHT_PARENTHESIS;

			case UNDERSCORE:
				return Keyboard.Key.UNDERSCORE;

			case WINDOWS:
				return Keyboard.Key.WINDOWS;

			case CONTEXT_MENU:
				return Keyboard.Key.CONTEXT_MENU;

			case FINAL:
				return Keyboard.Key.FINAL;

			case CONVERT:
				return Keyboard.Key.CONVERT;

			case NONCONVERT:
				return Keyboard.Key.NONCONVERT;

			case ACCEPT:
				return Keyboard.Key.ACCEPT;

			case MODECHANGE:
				return Keyboard.Key.MODECHANGE;

			case KANA:
				return Keyboard.Key.KANA;

			case KANJI:
				return Keyboard.Key.KANJI;

			case ALPHANUMERIC:
				return Keyboard.Key.ALPHANUMERIC;

			case KATAKANA:
				return Keyboard.Key.KATAKANA;

			case HIRAGANA:
				return Keyboard.Key.HIRAGANA;

			case FULL_WIDTH:
				return Keyboard.Key.FULL_WIDTH;

			case HALF_WIDTH:
				return Keyboard.Key.HALF_WIDTH;

			case ROMAN_CHARACTERS:
				return Keyboard.Key.ROMAN_CHARACTERS;

			case ALL_CANDIDATES:
				return Keyboard.Key.ALL_CANDIDATES;

			case PREVIOUS_CANDIDATE:
				return Keyboard.Key.PREVIOUS_CANDIDATE;

			case CODE_INPUT:
				return Keyboard.Key.CODE_INPUT;

			case JAPANESE_KATAKANA:
				return Keyboard.Key.JAPANESE_KATAKANA;

			case JAPANESE_HIRAGANA:
				return Keyboard.Key.JAPANESE_HIRAGANA;

			case JAPANESE_ROMAN:
				return Keyboard.Key.JAPANESE_ROMAN;

			case KANA_LOCK:
				return Keyboard.Key.KANA_LOCK;

			case INPUT_METHOD_ON_OFF:
				return Keyboard.Key.INPUT_METHOD_ON_OFF;

			case CUT:
				return Keyboard.Key.CUT;

			case COPY:
				return Keyboard.Key.COPY;

			case PASTE:
				return Keyboard.Key.PASTE;

			case UNDO:
				return Keyboard.Key.UNDO;

			case AGAIN:
				return Keyboard.Key.AGAIN;

			case FIND:
				return Keyboard.Key.FIND;

			case PROPS:
				return Keyboard.Key.PROPS;

			case STOP:
				return Keyboard.Key.STOP;

			case COMPOSE:
				return Keyboard.Key.COMPOSE;

			case ALT_GRAPH:
				return Keyboard.Key.ALT_GRAPH;

			case BEGIN:
				return Keyboard.Key.BEGIN;

			case UNDEFINED:
				return Keyboard.Key.UNDEFINED;

			case SOFTKEY_0:
				return Keyboard.Key.SOFTKEY_0;

			case SOFTKEY_1:
				return Keyboard.Key.SOFTKEY_1;

			case SOFTKEY_2:
				return Keyboard.Key.SOFTKEY_2;

			case SOFTKEY_3:
				return Keyboard.Key.SOFTKEY_3;

			case SOFTKEY_4:
				return Keyboard.Key.SOFTKEY_4;

			case SOFTKEY_5:
				return Keyboard.Key.SOFTKEY_5;

			case SOFTKEY_6:
				return Keyboard.Key.SOFTKEY_6;

			case SOFTKEY_7:
				return Keyboard.Key.SOFTKEY_7;

			case SOFTKEY_8:
				return Keyboard.Key.SOFTKEY_8;

			case SOFTKEY_9:
				return Keyboard.Key.SOFTKEY_9;

			case GAME_A:
				return Keyboard.Key.GAME_A;

			case GAME_B:
				return Keyboard.Key.GAME_B;

			case GAME_C:
				return Keyboard.Key.GAME_C;

			case GAME_D:
				return Keyboard.Key.GAME_D;

			case STAR:
				return Keyboard.Key.STAR;

			case POUND:
				return Keyboard.Key.POUND;

			case POWER:
				return Keyboard.Key.POWER;

			case INFO:
				return Keyboard.Key.INFO;

			case COLORED_KEY_0:
				return Keyboard.Key.COLORED_KEY_0;

			case COLORED_KEY_1:
				return Keyboard.Key.COLORED_KEY_1;

			case COLORED_KEY_2:
				return Keyboard.Key.COLORED_KEY_2;

			case COLORED_KEY_3:
				return Keyboard.Key.COLORED_KEY_3;

			case EJECT_TOGGLE:
				return Keyboard.Key.EJECT_TOGGLE;

			case PLAY:
				return Keyboard.Key.PLAY;

			case RECORD:
				return Keyboard.Key.RECORD;

			case FAST_FWD:
				return Keyboard.Key.FAST_FWD;

			case REWIND:
				return Keyboard.Key.REWIND;

			case TRACK_PREV:
				return Keyboard.Key.TRACK_PREV;

			case TRACK_NEXT:
				return Keyboard.Key.TRACK_NEXT;

			case CHANNEL_UP:
				return Keyboard.Key.CHANNEL_UP;

			case CHANNEL_DOWN:
				return Keyboard.Key.CHANNEL_DOWN;

			case VOLUME_UP:
				return Keyboard.Key.VOLUME_UP;

			case VOLUME_DOWN:
				return Keyboard.Key.VOLUME_DOWN;

			case MUTE:
				return Keyboard.Key.MUTE;

			case COMMAND:
				return Keyboard.Key.COMMAND;

			case SHORTCUT:
				return Keyboard.Key.SHORTCUT;

		}
		throw new AssertionError(String.format("Unknown key code %s", code));
	}
}
