package com.zarbosoft.bonestruct.editor.model.hid;

import com.zarbosoft.luxemj.Luxem;
import javafx.scene.input.KeyCode;

@Luxem.Configuration
public enum Key {
	@Luxem.Configuration(name = "enter")
	ENTER,

	@Luxem.Configuration(name = "backspace")
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
	SHORTCUT,;

	public static Key fromJFX(final KeyCode code) {
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
		throw new AssertionError(String.format("Unknown key code %s", code));
	}

	public static Key fromChar(final char at) {
		switch (at) {
			case 'a':
				return A;
			case 'b':
				return B;
			case 'c':
				return C;
			case 'd':
				return D;
			case 'e':
				return E;
			case 'f':
				return F;
			case 'g':
				return G;
			case 'h':
				return H;
			case 'i':
				return I;
			case 'j':
				return J;
			case 'k':
				return K;
			case 'l':
				return L;
			case 'm':
				return M;
			case 'n':
				return N;
			case 'o':
				return O;
			case 'p':
				return P;
			case 'q':
				return Q;
			case 'r':
				return R;
			case 's':
				return S;
			case 't':
				return T;
			case 'u':
				return U;
			case 'v':
				return V;
			case 'w':
				return W;
			case 'x':
				return X;
			case 'y':
				return Y;
			case 'z':
				return Z;
			case '-':
				return MINUS;
			case ':':
				return COLON;
		}
		throw new IllegalArgumentException();
	}
}
