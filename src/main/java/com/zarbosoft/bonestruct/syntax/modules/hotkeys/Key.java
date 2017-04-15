package com.zarbosoft.bonestruct.syntax.modules.hotkeys;

import com.zarbosoft.interface1.Configuration;
import javafx.scene.input.KeyCode;

@Configuration
public enum Key {
	@Configuration(name = "enter")
	ENTER,

	@Configuration(name = "backspace")
	BACK_SPACE,

	@Configuration(name = "tab")
	TAB,

	@Configuration(name = "cancel")
	CANCEL,

	@Configuration(name = "clear")
	CLEAR,

	@Configuration(name = "shift")
	SHIFT,

	@Configuration(name = "control")
	CONTROL,

	@Configuration(name = "alt")
	ALT,

	@Configuration(name = "pause")
	PAUSE,

	@Configuration(name = "caps")
	CAPS,

	@Configuration(name = "escape")
	ESCAPE,

	@Configuration(name = "space")
	SPACE,

	@Configuration(name = "page_up")
	PAGE_UP,

	@Configuration(name = "page_down")
	PAGE_DOWN,

	@Configuration(name = "end")
	END,

	@Configuration(name = "home")
	HOME,

	@Configuration(name = "left")
	LEFT,

	@Configuration(name = "up")
	UP,

	@Configuration(name = "right")
	RIGHT,

	@Configuration(name = "down")
	DOWN,

	@Configuration(name = "comma")
	COMMA,

	@Configuration(name = "minus")
	MINUS,

	@Configuration(name = "period")
	PERIOD,

	@Configuration(name = "slash")
	SLASH,

	@Configuration(name = "digit0")
	DIGIT0,

	@Configuration(name = "digit1")
	DIGIT1,

	@Configuration(name = "digit2")
	DIGIT2,

	@Configuration(name = "digit3")
	DIGIT3,

	@Configuration(name = "digit4")
	DIGIT4,

	@Configuration(name = "digit5")
	DIGIT5,

	@Configuration(name = "digit6")
	DIGIT6,

	@Configuration(name = "digit7")
	DIGIT7,

	@Configuration(name = "digit8")
	DIGIT8,

	@Configuration(name = "digit9")
	DIGIT9,

	@Configuration(name = "semicolon")
	SEMICOLON,

	@Configuration(name = "equals")
	EQUALS,

	@Configuration(name = "a")
	A,

	@Configuration(name = "b")
	B,

	@Configuration(name = "c")
	C,

	@Configuration(name = "d")
	D,

	@Configuration(name = "e")
	E,

	@Configuration(name = "f")
	F,

	@Configuration(name = "g")
	G,

	@Configuration(name = "h")
	H,

	@Configuration(name = "i")
	I,

	@Configuration(name = "j")
	J,

	@Configuration(name = "k")
	K,

	@Configuration(name = "l")
	L,

	@Configuration(name = "m")
	M,

	@Configuration(name = "n")
	N,

	@Configuration(name = "o")
	O,

	@Configuration(name = "p")
	P,

	@Configuration(name = "q")
	Q,

	@Configuration(name = "r")
	R,

	@Configuration(name = "s")
	S,

	@Configuration(name = "t")
	T,

	@Configuration(name = "u")
	U,

	@Configuration(name = "v")
	V,

	@Configuration(name = "w")
	W,

	@Configuration(name = "x")
	X,

	@Configuration(name = "y")
	Y,

	@Configuration(name = "z")
	Z,

	@Configuration(name = "open_bracket")
	OPEN_BRACKET,

	@Configuration(name = "back_slash")
	BACK_SLASH,

	@Configuration(name = "close_bracket")
	CLOSE_BRACKET,

	@Configuration(name = "numpad0")
	NUMPAD0,

	@Configuration(name = "numpad1")
	NUMPAD1,

	@Configuration(name = "numpad2")
	NUMPAD2,

	@Configuration(name = "numpad3")
	NUMPAD3,

	@Configuration(name = "numpad4")
	NUMPAD4,

	@Configuration(name = "numpad5")
	NUMPAD5,

	@Configuration(name = "numpad6")
	NUMPAD6,

	@Configuration(name = "numpad7")
	NUMPAD7,

	@Configuration(name = "numpad8")
	NUMPAD8,

	@Configuration(name = "numpad9")
	NUMPAD9,

	@Configuration(name = "multiply")
	MULTIPLY,

	@Configuration(name = "add")
	ADD,

	@Configuration(name = "separator")
	SEPARATOR,

	@Configuration(name = "subtract")
	SUBTRACT,

	@Configuration(name = "decimal")
	DECIMAL,

	@Configuration(name = "divide")
	DIVIDE,

	@Configuration(name = "delete")
	DELETE,

	@Configuration(name = "num_lock")
	NUM_LOCK,

	@Configuration(name = "scroll_lock")
	SCROLL_LOCK,

	@Configuration(name = "f1")
	F1,

	@Configuration(name = "f2")
	F2,

	@Configuration(name = "f3")
	F3,

	@Configuration(name = "f4")
	F4,

	@Configuration(name = "f5")
	F5,

	@Configuration(name = "f6")
	F6,

	@Configuration(name = "f7")
	F7,

	@Configuration(name = "f8")
	F8,

	@Configuration(name = "f9")
	F9,

	@Configuration(name = "f10")
	F10,

	@Configuration(name = "f11")
	F11,

	@Configuration(name = "f12")
	F12,

	@Configuration(name = "f13")
	F13,

	@Configuration(name = "f14")
	F14,

	@Configuration(name = "f15")
	F15,

	@Configuration(name = "f16")
	F16,

	@Configuration(name = "f17")
	F17,

	@Configuration(name = "f18")
	F18,

	@Configuration(name = "f19")
	F19,

	@Configuration(name = "f20")
	F20,

	@Configuration(name = "f21")
	F21,

	@Configuration(name = "f22")
	F22,

	@Configuration(name = "f23")
	F23,

	@Configuration(name = "f24")
	F24,

	@Configuration(name = "printscreen")
	PRINTSCREEN,

	@Configuration(name = "insert")
	INSERT,

	@Configuration(name = "help")
	HELP,

	@Configuration(name = "meta")
	META,

	@Configuration(name = "back_quote")
	BACK_QUOTE,

	@Configuration(name = "quote")
	QUOTE,

	@Configuration(name = "kp_up")
	KP_UP,

	@Configuration(name = "kp_down")
	KP_DOWN,

	@Configuration(name = "kp_left")
	KP_LEFT,

	@Configuration(name = "kp_right")
	KP_RIGHT,

	@Configuration(name = "dead_grave")
	DEAD_GRAVE,

	@Configuration(name = "dead_acute")
	DEAD_ACUTE,

	@Configuration(name = "dead_circumflex")
	DEAD_CIRCUMFLEX,

	@Configuration(name = "dead_tilde")
	DEAD_TILDE,

	@Configuration(name = "dead_macron")
	DEAD_MACRON,

	@Configuration(name = "dead_breve")
	DEAD_BREVE,

	@Configuration(name = "dead_abovedot")
	DEAD_ABOVEDOT,

	@Configuration(name = "dead_diaeresis")
	DEAD_DIAERESIS,

	@Configuration(name = "dead_abovering")
	DEAD_ABOVERING,

	@Configuration(name = "dead_doubleacute")
	DEAD_DOUBLEACUTE,

	@Configuration(name = "dead_caron")
	DEAD_CARON,

	@Configuration(name = "dead_cedilla")
	DEAD_CEDILLA,

	@Configuration(name = "dead_ogonek")
	DEAD_OGONEK,

	@Configuration(name = "dead_iota")
	DEAD_IOTA,

	@Configuration(name = "dead_voiced_sound")
	DEAD_VOICED_SOUND,

	@Configuration(name = "dead_semivoiced_sound")
	DEAD_SEMIVOICED_SOUND,

	@Configuration(name = "ampersand")
	AMPERSAND,

	@Configuration(name = "asterisk")
	ASTERISK,

	@Configuration(name = "quotedbl")
	QUOTEDBL,

	@Configuration(name = "less")
	LESS,

	@Configuration(name = "greater")
	GREATER,

	@Configuration(name = "braceleft")
	BRACELEFT,

	@Configuration(name = "braceright")
	BRACERIGHT,

	@Configuration(name = "at")
	AT,

	@Configuration(name = "colon")
	COLON,

	@Configuration(name = "circumflex")
	CIRCUMFLEX,

	@Configuration(name = "dollar")
	DOLLAR,

	@Configuration(name = "euro_sign")
	EURO_SIGN,

	@Configuration(name = "exclamation_mark")
	EXCLAMATION_MARK,

	@Configuration(name = "inverted_exclamation_mark")
	INVERTED_EXCLAMATION_MARK,

	@Configuration(name = "left_parenthesis")
	LEFT_PARENTHESIS,

	@Configuration(name = "number_sign")
	NUMBER_SIGN,

	@Configuration(name = "plus")
	PLUS,

	@Configuration(name = "right_parenthesis")
	RIGHT_PARENTHESIS,

	@Configuration(name = "underscore")
	UNDERSCORE,

	@Configuration(name = "windows")
	WINDOWS,

	@Configuration(name = "context_menu")
	CONTEXT_MENU,

	@Configuration(name = "final")
	FINAL,

	@Configuration(name = "convert")
	CONVERT,

	@Configuration(name = "nonconvert")
	NONCONVERT,

	@Configuration(name = "accept")
	ACCEPT,

	@Configuration(name = "modechange")
	MODECHANGE,

	@Configuration(name = "kana")
	KANA,

	@Configuration(name = "kanji")
	KANJI,

	@Configuration(name = "alphanumeric")
	ALPHANUMERIC,

	@Configuration(name = "katakana")
	KATAKANA,

	@Configuration(name = "hiragana")
	HIRAGANA,

	@Configuration(name = "full_width")
	FULL_WIDTH,

	@Configuration(name = "half_width")
	HALF_WIDTH,

	@Configuration(name = "roman_characters")
	ROMAN_CHARACTERS,

	@Configuration(name = "all_candidates")
	ALL_CANDIDATES,

	@Configuration(name = "previous_candidate")
	PREVIOUS_CANDIDATE,

	@Configuration(name = "code_input")
	CODE_INPUT,

	@Configuration(name = "japanese_katakana")
	JAPANESE_KATAKANA,

	@Configuration(name = "japanese_hiragana")
	JAPANESE_HIRAGANA,

	@Configuration(name = "japanese_roman")
	JAPANESE_ROMAN,

	@Configuration(name = "kana_lock")
	KANA_LOCK,

	@Configuration(name = "input_method_on_off")
	INPUT_METHOD_ON_OFF,

	@Configuration(name = "cut")
	CUT,

	@Configuration(name = "copy")
	COPY,

	@Configuration(name = "paste")
	PASTE,

	@Configuration(name = "undo")
	UNDO,

	@Configuration(name = "again")
	AGAIN,

	@Configuration(name = "find")
	FIND,

	@Configuration(name = "props")
	PROPS,

	@Configuration(name = "stop")
	STOP,

	@Configuration(name = "compose")
	COMPOSE,

	@Configuration(name = "alt_graph")
	ALT_GRAPH,

	@Configuration(name = "begin")
	BEGIN,

	@Configuration(name = "undefined")
	UNDEFINED,

	@Configuration(name = "softkey_0")
	SOFTKEY_0,

	@Configuration(name = "softkey_1")
	SOFTKEY_1,

	@Configuration(name = "softkey_2")
	SOFTKEY_2,

	@Configuration(name = "softkey_3")
	SOFTKEY_3,

	@Configuration(name = "softkey_4")
	SOFTKEY_4,

	@Configuration(name = "softkey_5")
	SOFTKEY_5,

	@Configuration(name = "softkey_6")
	SOFTKEY_6,

	@Configuration(name = "softkey_7")
	SOFTKEY_7,

	@Configuration(name = "softkey_8")
	SOFTKEY_8,

	@Configuration(name = "softkey_9")
	SOFTKEY_9,

	@Configuration(name = "game_a")
	GAME_A,

	@Configuration(name = "game_b")
	GAME_B,

	@Configuration(name = "game_c")
	GAME_C,

	@Configuration(name = "game_d")
	GAME_D,

	@Configuration(name = "star")
	STAR,

	@Configuration(name = "pound")
	POUND,

	@Configuration(name = "power")
	POWER,

	@Configuration(name = "info")
	INFO,

	@Configuration(name = "colored_key_0")
	COLORED_KEY_0,

	@Configuration(name = "colored_key_1")
	COLORED_KEY_1,

	@Configuration(name = "colored_key_2")
	COLORED_KEY_2,

	@Configuration(name = "colored_key_3")
	COLORED_KEY_3,

	@Configuration(name = "eject_toggle")
	EJECT_TOGGLE,

	@Configuration(name = "play")
	PLAY,

	@Configuration(name = "record")
	RECORD,

	@Configuration(name = "fast_fwd")
	FAST_FWD,

	@Configuration(name = "rewind")
	REWIND,

	@Configuration(name = "track_prev")
	TRACK_PREV,

	@Configuration(name = "track_next")
	TRACK_NEXT,

	@Configuration(name = "channel_up")
	CHANNEL_UP,

	@Configuration(name = "channel_down")
	CHANNEL_DOWN,

	@Configuration(name = "volume_up")
	VOLUME_UP,

	@Configuration(name = "volume_down")
	VOLUME_DOWN,

	@Configuration(name = "mute")
	MUTE,

	@Configuration(name = "command")
	COMMAND,

	@Configuration(name = "shortcut")
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
