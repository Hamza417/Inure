package app.simple.inure.decorations.emulatorview;

import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_BREAK;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_CAPS_LOCK;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_CTRL_LEFT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_CTRL_RIGHT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DEL;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DPAD_CENTER;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DPAD_DOWN;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DPAD_LEFT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DPAD_RIGHT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_DPAD_UP;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_ENTER;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_ESCAPE;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F1;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F10;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F11;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F12;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F2;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F3;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F4;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F5;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F6;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F7;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F8;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_F9;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_FORWARD_DEL;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_FUNCTION;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_INSERT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_MOVE_END;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_MOVE_HOME;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_0;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_1;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_2;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_3;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_4;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_5;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_6;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_7;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_8;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_9;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_ADD;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_COMMA;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_DIVIDE;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_DOT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_ENTER;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_EQUALS;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_MULTIPLY;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUMPAD_SUBTRACT;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_NUM_LOCK;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_PAGE_DOWN;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_PAGE_UP;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_SPACE;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_SYSRQ;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.KEYCODE_TAB;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.META_ALT_ON;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.META_CTRL_MASK;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.META_CTRL_ON;
import static app.simple.inure.decorations.emulatorview.compat.KeycodeConstants.META_SHIFT_ON;

/**
 * An ASCII key listener. Supports control characters and escape. Keeps track of
 * the current state of the alt, shift, fn, and control keys.
 */
class TermKeyListener {
    private final static String TAG = "TermKeyListener";
    private static final boolean LOG_MISC = true;
    private static final boolean LOG_KEYS = true;
    private static final boolean LOG_COMBINING_ACCENT = true;
    
    /**
     * Disabled for now because it interferes with ALT processing on phones with physical keyboards.
     */
    private final static boolean SUPPORT_8_BIT_META = false;
    
    private static final int KEYMOD_ALT = 0x80000000;
    private static final int KEYMOD_CTRL = 0x40000000;
    private static final int KEYMOD_SHIFT = 0x20000000;
    /**
     * Means this maps raw scancode
     */
    private static final int KEYMOD_SCAN = 0x10000000;
    
    private static Map <Integer, String> keymap;
    
    private final String[] keycodes = new String[256];
    private final String[] appKeyCodes = new String[256];
    
    /**
     * @noinspection CommentedOutCode
     */
    private void initKeyCodes() {
        keymap = new HashMap <>();
        keymap.put(KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;2D");
        keymap.put(KEYMOD_ALT | KEYCODE_DPAD_LEFT, "\033[1;3D");
        keymap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;4D");
        keymap.put(KEYMOD_CTRL | KEYCODE_DPAD_LEFT, "\033[1;5D");
        keymap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;6D");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_LEFT, "\033[1;7D");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_LEFT, "\033[1;8D");
        
        keymap.put(KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;2C");
        keymap.put(KEYMOD_ALT | KEYCODE_DPAD_RIGHT, "\033[1;3C");
        keymap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;4C");
        keymap.put(KEYMOD_CTRL | KEYCODE_DPAD_RIGHT, "\033[1;5C");
        keymap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;6C");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_RIGHT, "\033[1;7C");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_RIGHT, "\033[1;8C");
        
        keymap.put(KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;2A");
        keymap.put(KEYMOD_ALT | KEYCODE_DPAD_UP, "\033[1;3A");
        keymap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;4A");
        keymap.put(KEYMOD_CTRL | KEYCODE_DPAD_UP, "\033[1;5A");
        keymap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;6A");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_UP, "\033[1;7A");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_UP, "\033[1;8A");
        
        keymap.put(KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;2B");
        keymap.put(KEYMOD_ALT | KEYCODE_DPAD_DOWN, "\033[1;3B");
        keymap.put(KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;4B");
        keymap.put(KEYMOD_CTRL | KEYCODE_DPAD_DOWN, "\033[1;5B");
        keymap.put(KEYMOD_CTRL | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;6B");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYCODE_DPAD_DOWN, "\033[1;7B");
        keymap.put(KEYMOD_CTRL | KEYMOD_ALT | KEYMOD_SHIFT | KEYCODE_DPAD_DOWN, "\033[1;8B");
        
        //^[[3~
        keymap.put(KEYMOD_SHIFT | KEYCODE_FORWARD_DEL, "\033[3;2~");
        keymap.put(KEYMOD_ALT | KEYCODE_FORWARD_DEL, "\033[3;3~");
        keymap.put(KEYMOD_CTRL | KEYCODE_FORWARD_DEL, "\033[3;5~");
        
        //^[[2~
        keymap.put(KEYMOD_SHIFT | KEYCODE_INSERT, "\033[2;2~");
        keymap.put(KEYMOD_ALT | KEYCODE_INSERT, "\033[2;3~");
        keymap.put(KEYMOD_CTRL | KEYCODE_INSERT, "\033[2;5~");
        
        keymap.put(KEYMOD_CTRL | KEYCODE_MOVE_HOME, "\033[1;5H");
        keymap.put(KEYMOD_CTRL | KEYCODE_MOVE_END, "\033[1;5F");
        
        keymap.put(KEYMOD_ALT | KEYCODE_ENTER, "\033\r");
        keymap.put(KEYMOD_CTRL | KEYCODE_ENTER, "\n");
        // Duh, so special...
        keymap.put(KEYMOD_CTRL | KEYCODE_SPACE, "\000");
        
        keymap.put(KEYMOD_SHIFT | KEYCODE_F1, "\033[1;2P");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F2, "\033[1;2Q");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F3, "\033[1;2R");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F4, "\033[1;2S");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F5, "\033[15;2~");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F6, "\033[17;2~");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F7, "\033[18;2~");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F8, "\033[19;2~");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F9, "\033[20;2~");
        keymap.put(KEYMOD_SHIFT | KEYCODE_F10, "\033[21;2~");
        
        keycodes[KEYCODE_DPAD_CENTER] = "\015";
        keycodes[KEYCODE_DPAD_UP] = "\033[A";
        keycodes[KEYCODE_DPAD_DOWN] = "\033[B";
        keycodes[KEYCODE_DPAD_RIGHT] = "\033[C";
        keycodes[KEYCODE_DPAD_LEFT] = "\033[D";
        setFnKeys("vt100");
        keycodes[KEYCODE_SYSRQ] = "\033[32~"; // Sys Request / Print
        // Is this Scroll lock? mKeyCodes[Cancel] = "\033[33~";
        keycodes[KEYCODE_BREAK] = "\033[34~"; // Pause/Break
        
        keycodes[KEYCODE_TAB] = "\011";
        keycodes[KEYCODE_ENTER] = "\015";
        keycodes[KEYCODE_ESCAPE] = "\033";
        
        keycodes[KEYCODE_INSERT] = "\033[2~";
        keycodes[KEYCODE_FORWARD_DEL] = "\033[3~";
        // Home/End keys are set by setFnKeys()
        keycodes[KEYCODE_PAGE_UP] = "\033[5~";
        keycodes[KEYCODE_PAGE_DOWN] = "\033[6~";
        keycodes[KEYCODE_DEL] = "\177";
        keycodes[KEYCODE_NUM_LOCK] = "\033OP";
        keycodes[KEYCODE_NUMPAD_DIVIDE] = "/";
        keycodes[KEYCODE_NUMPAD_MULTIPLY] = "*";
        keycodes[KEYCODE_NUMPAD_SUBTRACT] = "-";
        keycodes[KEYCODE_NUMPAD_ADD] = "+";
        keycodes[KEYCODE_NUMPAD_ENTER] = "\015";
        keycodes[KEYCODE_NUMPAD_EQUALS] = "=";
        keycodes[KEYCODE_NUMPAD_COMMA] = ",";
/*
        mKeyCodes[KEYCODE_NUMPAD_DOT] = ".";
        mKeyCodes[KEYCODE_NUMPAD_0] = "0";
        mKeyCodes[KEYCODE_NUMPAD_1] = "1";
        mKeyCodes[KEYCODE_NUMPAD_2] = "2";
        mKeyCodes[KEYCODE_NUMPAD_3] = "3";
        mKeyCodes[KEYCODE_NUMPAD_4] = "4";
        mKeyCodes[KEYCODE_NUMPAD_5] = "5";
        mKeyCodes[KEYCODE_NUMPAD_6] = "6";
        mKeyCodes[KEYCODE_NUMPAD_7] = "7";
        mKeyCodes[KEYCODE_NUMPAD_8] = "8";
        mKeyCodes[KEYCODE_NUMPAD_9] = "9";
*/
        // Keypad is used for cursor/func keys
        keycodes[KEYCODE_NUMPAD_DOT] = keycodes[KEYCODE_FORWARD_DEL];
        keycodes[KEYCODE_NUMPAD_0] = keycodes[KEYCODE_INSERT];
        keycodes[KEYCODE_NUMPAD_1] = keycodes[KEYCODE_MOVE_END];
        keycodes[KEYCODE_NUMPAD_2] = keycodes[KEYCODE_DPAD_DOWN];
        keycodes[KEYCODE_NUMPAD_3] = keycodes[KEYCODE_PAGE_DOWN];
        keycodes[KEYCODE_NUMPAD_4] = keycodes[KEYCODE_DPAD_LEFT];
        keycodes[KEYCODE_NUMPAD_5] = "5";
        keycodes[KEYCODE_NUMPAD_6] = keycodes[KEYCODE_DPAD_RIGHT];
        keycodes[KEYCODE_NUMPAD_7] = keycodes[KEYCODE_MOVE_HOME];
        keycodes[KEYCODE_NUMPAD_8] = keycodes[KEYCODE_DPAD_UP];
        keycodes[KEYCODE_NUMPAD_9] = keycodes[KEYCODE_PAGE_UP];
        
        //        mAppKeyCodes[KEYCODE_DPAD_UP] = "\033OA";
        //        mAppKeyCodes[KEYCODE_DPAD_DOWN] = "\033OB";
        //        mAppKeyCodes[KEYCODE_DPAD_RIGHT] = "\033OC";
        //        mAppKeyCodes[KEYCODE_DPAD_LEFT] = "\033OD";
        appKeyCodes[KEYCODE_NUMPAD_DIVIDE] = "\033Oo";
        appKeyCodes[KEYCODE_NUMPAD_MULTIPLY] = "\033Oj";
        appKeyCodes[KEYCODE_NUMPAD_SUBTRACT] = "\033Om";
        appKeyCodes[KEYCODE_NUMPAD_ADD] = "\033Ok";
        appKeyCodes[KEYCODE_NUMPAD_ENTER] = "\033OM";
        appKeyCodes[KEYCODE_NUMPAD_EQUALS] = "\033OX";
        appKeyCodes[KEYCODE_NUMPAD_DOT] = "\033On";
        appKeyCodes[KEYCODE_NUMPAD_COMMA] = "\033Ol";
        appKeyCodes[KEYCODE_NUMPAD_0] = "\033Op";
        appKeyCodes[KEYCODE_NUMPAD_1] = "\033Oq";
        appKeyCodes[KEYCODE_NUMPAD_2] = "\033Or";
        appKeyCodes[KEYCODE_NUMPAD_3] = "\033Os";
        appKeyCodes[KEYCODE_NUMPAD_4] = "\033Ot";
        appKeyCodes[KEYCODE_NUMPAD_5] = "\033Ou";
        appKeyCodes[KEYCODE_NUMPAD_6] = "\033Ov";
        appKeyCodes[KEYCODE_NUMPAD_7] = "\033Ow";
        appKeyCodes[KEYCODE_NUMPAD_8] = "\033Ox";
        appKeyCodes[KEYCODE_NUMPAD_9] = "\033Oy";
    }
    
    public void setCursorKeysApplicationMode(boolean val) {
        if (LOG_MISC) {
            Log.d(EmulatorDebug.LOG_TAG, "CursorKeysApplicationMode=" + val);
        }
        if (val) {
            keycodes[KEYCODE_NUMPAD_8] = keycodes[KEYCODE_DPAD_UP] = "\033OA";
            keycodes[KEYCODE_NUMPAD_2] = keycodes[KEYCODE_DPAD_DOWN] = "\033OB";
            keycodes[KEYCODE_NUMPAD_6] = keycodes[KEYCODE_DPAD_RIGHT] = "\033OC";
            keycodes[KEYCODE_NUMPAD_4] = keycodes[KEYCODE_DPAD_LEFT] = "\033OD";
        } else {
            keycodes[KEYCODE_NUMPAD_8] = keycodes[KEYCODE_DPAD_UP] = "\033[A";
            keycodes[KEYCODE_NUMPAD_2] = keycodes[KEYCODE_DPAD_DOWN] = "\033[B";
            keycodes[KEYCODE_NUMPAD_6] = keycodes[KEYCODE_DPAD_RIGHT] = "\033[C";
            keycodes[KEYCODE_NUMPAD_4] = keycodes[KEYCODE_DPAD_LEFT] = "\033[D";
        }
    }
    
    /**
     * The state engine for a modifier key. Can be pressed, released, locked,
     * and so on.
     */
    private static class ModifierKey {
        
        private int state;
        
        private static final int UNPRESSED = 0;
        
        private static final int PRESSED = 1;
        
        private static final int RELEASED = 2;
        
        private static final int USED = 3;
        
        private static final int LOCKED = 4;
        
        /**
         * Construct a modifier key. UNPRESSED by default.
         */
        public ModifierKey() {
            state = UNPRESSED;
        }
        
        public void onPress() {
            switch (state) {
                case PRESSED:
                    // This is a repeat before use
                    break;
                case RELEASED:
                    state = LOCKED;
                    break;
                case USED:
                    // This is a repeat after use
                    break;
                case LOCKED:
                    state = UNPRESSED;
                    break;
                default:
                    state = PRESSED;
                    break;
            }
        }
        
        public void onRelease() {
            switch (state) {
                case USED:
                    state = UNPRESSED;
                    break;
                case PRESSED:
                    state = RELEASED;
                    break;
                default:
                    // Leave state alone
                    break;
            }
        }
        
        public void adjustAfterKeypress() {
            switch (state) {
                case PRESSED:
                    state = USED;
                    break;
                case RELEASED:
                    state = UNPRESSED;
                    break;
                default:
                    // Leave state alone
                    break;
            }
        }
        
        public boolean isActive() {
            return state != UNPRESSED;
        }
        
        public int getUIMode() {
            return switch (state) {
                case PRESSED,
                     RELEASED,
                     USED ->
                        TextRenderer.MODE_ON;
                case LOCKED ->
                        TextRenderer.MODE_LOCKED;
                default -> // UNPRESSED
                        TextRenderer.MODE_OFF;
            };
        }
    }
    
    private final ModifierKey altKey = new ModifierKey();
    
    private final ModifierKey capKey = new ModifierKey();
    
    private final ModifierKey controlKey = new ModifierKey();
    
    private final ModifierKey fnKey = new ModifierKey();
    
    private int cursorMode;
    
    private boolean hardwareControlKey;
    
    private final TermSession termSession;
    
    private int backKeyCode;
    private boolean altSendsEsc;
    
    private int combiningAccent;
    
    // Map keycodes out of (above) the Unicode code point space.
    static public final int KEYCODE_OFFSET = 0xA00000;
    
    /**
     * Construct a term key listener.
     */
    public TermKeyListener(TermSession termSession) {
        this.termSession = termSession;
        initKeyCodes();
        updateCursorMode();
    }
    
    public void setBackKeyCharacter(int code) {
        backKeyCode = code;
    }
    
    public void setAltSendsEsc(boolean flag) {
        altSendsEsc = flag;
    }
    
    public void handleHardwareControlKey(boolean down) {
        hardwareControlKey = down;
    }
    
    public void onPause() {
        // Ensure we don't have any left-over modifier state when switching
        // views.
        hardwareControlKey = false;
    }
    
    public void onResume() {
        // Nothing special.
    }
    
    public void handleControlKey(boolean down) {
        if (down) {
            controlKey.onPress();
        } else {
            controlKey.onRelease();
        }
        updateCursorMode();
    }
    
    public void handleFnKey(boolean down) {
        if (down) {
            fnKey.onPress();
            Log.i(TAG, "FnKey pressed, mode: " + fnKey.getUIMode());
        } else {
            fnKey.onRelease();
            Log.i(TAG, "FnKey released, mode: " + fnKey.getUIMode());
        }
        updateCursorMode();
    }
    
    public void setTermType(String termType) {
        setFnKeys(termType);
    }
    
    private void setFnKeys(String termType) {
        // These key assignments taken from the debian squeeze terminfo database.
        if (termType.equals("xterm")) {
            keycodes[KEYCODE_NUMPAD_7] = keycodes[KEYCODE_MOVE_HOME] = "\033OH";
            keycodes[KEYCODE_NUMPAD_1] = keycodes[KEYCODE_MOVE_END] = "\033OF";
        } else {
            keycodes[KEYCODE_NUMPAD_7] = keycodes[KEYCODE_MOVE_HOME] = "\033[1~";
            keycodes[KEYCODE_NUMPAD_1] = keycodes[KEYCODE_MOVE_END] = "\033[4~";
        }
        if (termType.equals("vt100")) {
            keycodes[KEYCODE_F1] = "\033OP"; // VT100 PF1
            keycodes[KEYCODE_F2] = "\033OQ"; // VT100 PF2
            keycodes[KEYCODE_F3] = "\033OR"; // VT100 PF3
            keycodes[KEYCODE_F4] = "\033OS"; // VT100 PF4
            // the following keys are in the database, but aren't on a real vt100.
            keycodes[KEYCODE_F5] = "\033Ot";
            keycodes[KEYCODE_F6] = "\033Ou";
            keycodes[KEYCODE_F7] = "\033Ov";
            keycodes[KEYCODE_F8] = "\033Ol";
            keycodes[KEYCODE_F9] = "\033Ow";
            keycodes[KEYCODE_F10] = "\033Ox";
            // The following keys are not in database.
            keycodes[KEYCODE_F11] = "\033[23~";
            keycodes[KEYCODE_F12] = "\033[24~";
        } else if (termType.startsWith("linux")) {
            keycodes[KEYCODE_F1] = "\033[[A";
            keycodes[KEYCODE_F2] = "\033[[B";
            keycodes[KEYCODE_F3] = "\033[[C";
            keycodes[KEYCODE_F4] = "\033[[D";
            keycodes[KEYCODE_F5] = "\033[[E";
            keycodes[KEYCODE_F6] = "\033[17~";
            keycodes[KEYCODE_F7] = "\033[18~";
            keycodes[KEYCODE_F8] = "\033[19~";
            keycodes[KEYCODE_F9] = "\033[20~";
            keycodes[KEYCODE_F10] = "\033[21~";
            keycodes[KEYCODE_F11] = "\033[23~";
            keycodes[KEYCODE_F12] = "\033[24~";
        } else {
            // default
            // screen, screen-256colors, xterm, anything new
            keycodes[KEYCODE_F1] = "\033OP"; // VT100 PF1
            keycodes[KEYCODE_F2] = "\033OQ"; // VT100 PF2
            keycodes[KEYCODE_F3] = "\033OR"; // VT100 PF3
            keycodes[KEYCODE_F4] = "\033OS"; // VT100 PF4
            keycodes[KEYCODE_F5] = "\033[15~";
            keycodes[KEYCODE_F6] = "\033[17~";
            keycodes[KEYCODE_F7] = "\033[18~";
            keycodes[KEYCODE_F8] = "\033[19~";
            keycodes[KEYCODE_F9] = "\033[20~";
            keycodes[KEYCODE_F10] = "\033[21~";
            keycodes[KEYCODE_F11] = "\033[23~";
            keycodes[KEYCODE_F12] = "\033[24~";
        }
    }
    
    public int mapControlChar(int ch) {
        return mapControlChar(hardwareControlKey || controlKey.isActive(), fnKey.isActive(), ch);
    }
    
    public int mapControlChar(boolean control, boolean fn, int ch) {
        int result = ch;
        Log.i(TAG, "mapControlChar(" + control + "," + fn + "," + ch + ")");
        
        if (control) {
            // Search is the control key.
            if (result >= 'a' && result <= 'z') {
                result = (char) (result - 'a' + '\001');
            } else if (result >= 'A' && result <= 'Z') {
                result = (char) (result - 'A' + '\001');
            } else if (result == ' ' || result == '2') {
                result = 0;
            } else if (result == '[' || result == '3') {
                result = 27; // ^[ (Esc)
            } else if (result == '\\' || result == '4') {
                result = 28;
            } else if (result == ']' || result == '5') {
                result = 29;
            } else if (result == '^' || result == '6') {
                result = 30; // control-^
            } else if (result == '_' || result == '7') {
                result = 31;
            } else if (result == '8') {
                result = 127; // DEL
            } else if (result == '9') {
                result = KEYCODE_OFFSET + KEYCODE_F11;
            } else if (result == '0') {
                result = KEYCODE_OFFSET + KEYCODE_F12;
            }
        } else if (fn) {
            if (result == 'w' || result == 'W') {
                result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_UP;
            } else if (result == 'a' || result == 'A') {
                result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_LEFT;
            } else if (result == 's' || result == 'S') {
                result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_DOWN;
            } else if (result == 'd' || result == 'D') {
                result = KEYCODE_OFFSET + KeyEvent.KEYCODE_DPAD_RIGHT;
            } else if (result == 'p' || result == 'P') {
                result = KEYCODE_OFFSET + KEYCODE_PAGE_UP;
            } else if (result == 'n' || result == 'N') {
                result = KEYCODE_OFFSET + KEYCODE_PAGE_DOWN;
            } else if (result == 't' || result == 'T') {
                result = KEYCODE_OFFSET + KeyEvent.KEYCODE_TAB;
            } else if (result == 'l' || result == 'L') {
                result = '|';
            } else if (result == 'u' || result == 'U') {
                result = '_';
            } else if (result == 'e' || result == 'E') {
                result = 27; // ^[ (Esc)
            } else if (result == '.') {
                result = 28; // ^\
            } else if (result > '0' && result <= '9') {
                // F1-F9
                result = (char) (result + KEYCODE_OFFSET + KEYCODE_F1 - 1);
            } else if (result == '0') {
                result = KEYCODE_OFFSET + KEYCODE_F10;
            } else if (result == 'i' || result == 'I') {
                result = KEYCODE_OFFSET + KEYCODE_INSERT;
            } else if (result == 'x' || result == 'X') {
                result = KEYCODE_OFFSET + KEYCODE_FORWARD_DEL;
            } else if (result == 'h' || result == 'H') {
                result = KEYCODE_OFFSET + KEYCODE_MOVE_HOME;
            } else if (result == 'f' || result == 'F') {
                result = KEYCODE_OFFSET + KEYCODE_MOVE_END;
            }
        }
        
        if (result > -1) {
            altKey.adjustAfterKeypress();
            capKey.adjustAfterKeypress();
            controlKey.adjustAfterKeypress();
            fnKey.adjustAfterKeypress();
            updateCursorMode();
        }
        
        return result;
    }
    
    /**
     * Handle a keyDown event.
     *
     * @param keyCode the keycode of the keyDown event
     */
    public void keyDown(int keyCode, KeyEvent event, boolean appMode, boolean allowToggle) throws IOException {
        if (LOG_KEYS) {
            Log.i(TAG, "keyDown(" + keyCode + "," + event + "," + appMode + "," + allowToggle + ")");
        }
        if (handleKeyCode(keyCode, event, appMode)) {
            if (LOG_KEYS) {
                Log.i(TAG, "keyDown handled by keyCode: " + keyCode);
            }
            return;
        }
        
        int result = -1;
        boolean chordedCtrl = false;
        boolean setHighBit = false;
        switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_ALT_LEFT:
                if (allowToggle) {
                    altKey.onPress();
                    updateCursorMode();
                }
                break;
            
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (allowToggle) {
                    capKey.onPress();
                    updateCursorMode();
                }
                break;
            
            case KEYCODE_CTRL_LEFT:
            case KEYCODE_CTRL_RIGHT:
                // Ignore the control key.
                return;
            
            case KEYCODE_CAPS_LOCK:
                // Ignore the capslock key.
                return;
            
            case KEYCODE_FUNCTION:
                // Ignore the function key.
                return;
            
            case KeyEvent.KEYCODE_BACK:
                result = backKeyCode;
                break;
            
            default: {
                int metaState = event.getMetaState();
                chordedCtrl = ((META_CTRL_ON & metaState) != 0);
                boolean effectiveCaps = allowToggle &&
                        (capKey.isActive());
                boolean effectiveAlt = allowToggle && altKey.isActive();
                int effectiveMetaState = metaState & (~META_CTRL_MASK);
                if (effectiveCaps) {
                    effectiveMetaState |= KeyEvent.META_SHIFT_ON;
                }
                if (!allowToggle && (effectiveMetaState & META_ALT_ON) != 0) {
                    effectiveAlt = true;
                }
                if (effectiveAlt) {
                    if (altSendsEsc) {
                        termSession.write(new byte[] {0x1b}, 0, 1);
                        effectiveMetaState &= ~KeyEvent.META_ALT_MASK;
                    } else if (SUPPORT_8_BIT_META) {
                        setHighBit = true;
                        effectiveMetaState &= ~KeyEvent.META_ALT_MASK;
                    } else {
                        // Legacy behavior: Pass Alt through to allow composing characters.
                        effectiveMetaState |= KeyEvent.META_ALT_ON;
                    }
                }
                
                // Note: The Hacker keyboard IME key labeled Alt actually sends Meta.
                
                if ((metaState & KeyEvent.META_META_ON) != 0) {
                    if (altSendsEsc) {
                        termSession.write(new byte[] {0x1b}, 0, 1);
                        effectiveMetaState &= ~KeyEvent.META_META_MASK;
                    } else {
                        if (SUPPORT_8_BIT_META) {
                            setHighBit = true;
                            effectiveMetaState &= ~KeyEvent.META_META_MASK;
                        }
                    }
                }
                
                result = event.getUnicodeChar(effectiveMetaState);
                
                if ((result & KeyCharacterMap.COMBINING_ACCENT) != 0) {
                    if (LOG_COMBINING_ACCENT) {
                        Log.i(TAG, "Got combining accent " + result);
                    }
                    combiningAccent = result & KeyCharacterMap.COMBINING_ACCENT_MASK;
                    return;
                }
                
                if (combiningAccent != 0) {
                    int unaccentedChar = result;
                    result = KeyCharacterMap.getDeadChar(combiningAccent, unaccentedChar);
                    if (LOG_COMBINING_ACCENT) {
                        Log.i(TAG, "getDeadChar(" + combiningAccent + ", " + unaccentedChar + ") -> " + result);
                    }
                    combiningAccent = 0;
                }
                
                break;
            }
        }
        
        boolean effectiveControl = chordedCtrl || hardwareControlKey || (allowToggle && controlKey.isActive());
        Log.i(TAG, "effectiveControl=" + effectiveControl + ", chordedCtrl=" + chordedCtrl + ", " +
                "hardwareControlKey=" + hardwareControlKey + ", controlKey.isActive()=" + controlKey.isActive() +
                " fnKey.isActive()=" + fnKey.isActive() + ", allowToggle=" + allowToggle);
        
        boolean effectiveFn = allowToggle && fnKey.isActive();
        
        result = mapControlChar(effectiveControl, effectiveFn, result);
        
        if (result >= KEYCODE_OFFSET) {
            handleKeyCode(result - KEYCODE_OFFSET, null, appMode);
        } else if (result >= 0) {
            if (setHighBit) {
                result |= 0x80;
            }
            termSession.write(result);
        }
    }
    
    public int getCombiningAccent() {
        return combiningAccent;
    }
    
    public int getCursorMode() {
        return cursorMode;
    }
    
    private void updateCursorMode() {
        cursorMode = getCursorModeHelper(capKey, TextRenderer.MODE_SHIFT_SHIFT)
                | getCursorModeHelper(altKey, TextRenderer.MODE_ALT_SHIFT)
                | getCursorModeHelper(controlKey, TextRenderer.MODE_CTRL_SHIFT)
                | getCursorModeHelper(fnKey, TextRenderer.MODE_FN_SHIFT);
    }
    
    private static int getCursorModeHelper(ModifierKey key, int shift) {
        return key.getUIMode() << shift;
    }
    
    // Checks if the input event comes from a device that supports toggle/chorded modifier keys.
    // In practice, this function almost always returns true because:
    // - Most modern Android devices and IMEs do not provide accurate modifier behavior info.
    // - If device info is missing or an error occurs, the fallback is to allow toggle (return true).
    // - Even when device info is present, most virtual keyboards do not report toggle/chorded support,
    //   but the fallback still enables toggle mode for compatibility.
    static boolean isEventFromToggleDevice(KeyEvent event) {
        Log.i(TAG, "isEventFromToggleDevice: event=" + event);
        Log.v(TAG, "isEventFromToggleDevice: Should toggle modifier keys be allowed? " +
                "This is a fallback check, as most devices do not provide accurate info.");
        return true;
    }
    
    /**
     * @noinspection RedundantThrows
     */
    public boolean handleKeyCode(int keyCode, KeyEvent event, boolean appMode) throws IOException {
        String code = null;
        if (event != null) {
            int keyMod = getKeyMod(event);
            // First try to map scancode
            code = keymap.get(event.getScanCode() | KEYMOD_SCAN | keyMod);
            if (code == null) {
                code = keymap.get(keyCode | keyMod);
            }
        }
        
        if (code == null && keyCode >= 0 && keyCode < keycodes.length) {
            if (appMode) {
                code = appKeyCodes[keyCode];
            }
            if (code == null) {
                code = keycodes[keyCode];
            }
        }
        
        if (code != null) {
            if (EmulatorDebug.LOG_CHARACTERS_FLAG) {
                byte[] bytes = code.getBytes();
                Log.d(EmulatorDebug.LOG_TAG, "Out: '" + EmulatorDebug.bytesToString(bytes, 0, bytes.length) + "'");
            }
            termSession.write(code);
            return true;
        }
        return false;
    }
    
    private int getKeyMod(KeyEvent event) {
        int keyMod = 0;
        // META_CTRL_ON was added only in API 11, so don't use it,
        // use our own tracking of Ctrl key instead.
        // (event.getMetaState() & META_CTRL_ON) != 0
        if (hardwareControlKey || controlKey.isActive()) {
            keyMod |= KEYMOD_CTRL;
        }
        if ((event.getMetaState() & META_ALT_ON) != 0) {
            keyMod |= KEYMOD_ALT;
        }
        if ((event.getMetaState() & META_SHIFT_ON) != 0) {
            keyMod |= KEYMOD_SHIFT;
        }
        return keyMod;
    }
    
    /**
     * Handle a keyUp event.
     *
     * @param keyCode the keyCode of the keyUp event
     */
    public void keyUp(int keyCode, KeyEvent event) {
        boolean allowToggle = isEventFromToggleDevice(event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
                if (allowToggle) {
                    altKey.onRelease();
                    updateCursorMode();
                }
                break;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (allowToggle) {
                    capKey.onRelease();
                    updateCursorMode();
                }
                break;
            
            case KEYCODE_CTRL_LEFT:
            case KEYCODE_CTRL_RIGHT:
                // ignore control keys.
                break;
            
            default:
                // Ignore other keyUps
                break;
        }
    }
    
    public boolean getAltSendsEsc() {
        return altSendsEsc;
    }
    
    public boolean isAltActive() {
        return altKey.isActive();
    }
    
    public boolean isCtrlActive() {
        return controlKey.isActive();
    }
}
