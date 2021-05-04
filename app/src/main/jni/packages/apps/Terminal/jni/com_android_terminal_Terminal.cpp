/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "Terminal"

//#include "../../../../jni.h"
//#include "../../../../../../../../../../Android_SDK/ndk/20.0.5471264/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h"
//#include "../../../../libnativehelper/include/nativehelper/JNIHelp.h"
#include "../../../../system/core/libutils/include/utils/Log.h"
#include "../../../../system/core/libutils/include/utils/Mutex.h"
#include "../../../../android_runtime/AndroidRuntime.h"
#include <jni.h>
#include "../../../../libnativehelper/include/nativehelper/ScopedLocalRef.h"
#include "../../../../libnativehelper/include/nativehelper/ScopedPrimitiveArray.h"

#include <fcntl.h>
#include <pty.h>
#include <stdio.h>
#include <termios.h>
#include <unistd.h>

#include "../../../../external/libvterm/include/vterm.h"
#include <string.h>

#define USE_TEST_SHELL 0
#define DEBUG_CALLBACKS 0
#define DEBUG_IO 0
#define DEBUG_SCROLLBACK 0
#ifndef NELEM // from ../../../../libnativehelper/include/nativehelper/JNIHelp.h
# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
#endif

namespace JVM {
    extern JavaVM* jvm;
    extern JNIEnv* GetEnv();
}

namespace android {

/*
 * Callback class reference
 */
static jclass terminalCallbacksClass;

/*
 * Callback methods
 */
static jmethodID damageMethod;
static jmethodID moveRectMethod;
static jmethodID moveCursorMethod;
static jmethodID setTermPropBooleanMethod;
static jmethodID setTermPropIntMethod;
static jmethodID setTermPropStringMethod;
static jmethodID setTermPropColorMethod;
static jmethodID bellMethod;

/*
 * CellRun class
 */
static jclass cellRunClass;
static jfieldID cellRunDataField;
static jfieldID cellRunDataSizeField;
static jfieldID cellRunColSizeField;
static jfieldID cellRunFgField;
static jfieldID cellRunBgField;

typedef short unsigned int dimen_t;

class ScrollbackLine {
public:
    inline ScrollbackLine(dimen_t _cols) : cols(_cols) {
        mCells = new VTermScreenCell[cols];
    };
    inline ~ScrollbackLine() {
        delete mCells;
    }

    inline dimen_t copyFrom(dimen_t cols, const VTermScreenCell* cells) {
        dimen_t n = this->cols > cols ? cols : this->cols;
        memcpy(mCells, cells, sizeof(VTermScreenCell) * n);
        return n;
    }

    inline dimen_t copyTo(dimen_t cols, VTermScreenCell* cells) {
        dimen_t n = cols > this->cols ? this->cols : cols;
        memcpy(cells, mCells, sizeof(VTermScreenCell) * n);
        return n;
    }

    inline void getCell(dimen_t col, VTermScreenCell* cell) {
        *cell = mCells[col];
    }

    const dimen_t cols;

private:
    VTermScreenCell* mCells;
};

/*
 * Terminal session
 */
class Terminal {
public:
    Terminal(jobject callbacks);
    ~Terminal();

    status_t run();

    size_t write(const char *bytes, size_t len);

    bool dispatchCharacter(int mod, int character);
    bool dispatchKey(int mod, int key);
    bool flushInput();

    status_t resize(dimen_t rows, dimen_t cols, dimen_t scrollRows);

    status_t onPushline(dimen_t cols, const VTermScreenCell* cells);
    status_t onPopline(dimen_t cols, VTermScreenCell* cells);

    void getCellLocked(VTermPos pos, VTermScreenCell* cell);

    dimen_t getRows() const;
    dimen_t getCols() const;
    dimen_t getScrollRows() const;

    jobject getCallbacks() const;

    // Lock protecting mutations of internal libvterm state
    Mutex mLock;

private:
    int mMasterFd;
    pid_t mChildPid;
    VTerm *mVt;
    VTermScreen *mVts;

    jobject mCallbacks;

    dimen_t mRows;
    dimen_t mCols;
    bool mKilled;

    ScrollbackLine **mScroll;
    dimen_t mScrollCur;
    dimen_t mScrollSize;

};

/*
 * VTerm event handlers
 */

static int term_damage(VTermRect rect, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_damage");
#endif

    return JVM::GetEnv()->CallIntMethod(term->getCallbacks(), damageMethod, rect.start_row, rect.end_row,
            rect.start_col, rect.end_col);
}

static int term_moverect(VTermRect dest, VTermRect src, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_moverect");
#endif

    return JVM::GetEnv()->CallIntMethod(term->getCallbacks(), moveRectMethod,
            dest.start_row, dest.end_row, dest.start_col, dest.end_col,
            src.start_row, src.end_row, src.start_col, src.end_col);
}

static int term_movecursor(VTermPos pos, VTermPos oldpos, int visible, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_movecursor");
#endif

    return JVM::GetEnv()->CallIntMethod(term->getCallbacks(), moveCursorMethod, pos.row,
            pos.col, oldpos.row, oldpos.col, visible);
}

static int term_settermprop(VTermProp prop, VTermValue *val, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_settermprop");
#endif

    JNIEnv* env = JVM::GetEnv();
    switch (vterm_get_prop_type(prop)) {
    case VTERM_VALUETYPE_BOOL:
        return env->CallIntMethod(term->getCallbacks(), setTermPropBooleanMethod, prop,
                val->boolean ? JNI_TRUE : JNI_FALSE);
    case VTERM_VALUETYPE_INT:
        return env->CallIntMethod(term->getCallbacks(), setTermPropIntMethod, prop, val->number);
    case VTERM_VALUETYPE_STRING:
        return env->CallIntMethod(term->getCallbacks(), setTermPropStringMethod, prop,
                env->NewStringUTF(val->string));
    case VTERM_VALUETYPE_COLOR:
        return env->CallIntMethod(term->getCallbacks(), setTermPropIntMethod, prop, val->color.red,
                val->color.green, val->color.blue);
    default:
        ALOGE("unknown callback type");
        return 0;
    }
}

static int term_setmousefunc(VTermMouseFunc func, void *data, void *user) {
#if DEBUG_CALLBACKS
    ALOGW("term_setmousefunc");
#endif
    return 1;
}

static int term_bell(void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_bell");
#endif

    JNIEnv* env = JVM::GetEnv();
    return env->CallIntMethod(term->getCallbacks(), bellMethod);
}

static int term_sb_pushline(int cols, const VTermScreenCell *cells, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_sb_pushline");
#endif

    return term->onPushline(cols, cells);
}

static int term_sb_popline(int cols, VTermScreenCell *cells, void *user) {
    Terminal* term = reinterpret_cast<Terminal*>(user);
#if DEBUG_CALLBACKS
    ALOGW("term_sb_popline");
#endif

    return term->onPopline(cols, cells);
}

static VTermScreenCallbacks cb = {
    .damage = term_damage,
    .moverect = term_moverect,
    .movecursor = term_movecursor,
    .settermprop = term_settermprop,
    .setmousefunc = term_setmousefunc,
    .bell = term_bell,
    // Resize requests are applied immediately, so callback is ignored
    .resize = NULL,
    .sb_pushline = term_sb_pushline,
    .sb_popline = term_sb_popline,
};

Terminal::Terminal(jobject callbacks) :
        mCallbacks(callbacks), mRows(25), mCols(80), mKilled(false),
        mScrollCur(0), mScrollSize(100) {
    JNIEnv* env = JVM::GetEnv();
    mCallbacks = env->NewGlobalRef(callbacks);

    mScroll = new ScrollbackLine*[mScrollSize];
    memset(mScroll, 0, sizeof(ScrollbackLine*) * mScrollSize);

    /* Create VTerm */
    mVt = vterm_new(mRows, mCols);
    vterm_parser_set_utf8(mVt, 1);

    /* Set up screen */
    mVts = vterm_obtain_screen(mVt);
    vterm_screen_enable_altscreen(mVts, 1);
    vterm_screen_set_callbacks(mVts, &cb, this);
    vterm_screen_set_damage_merge(mVts, VTERM_DAMAGE_SCROLL);
    vterm_screen_reset(mVts, 1);
}

Terminal::~Terminal() {
    close(mMasterFd);
    ::kill(mChildPid, SIGHUP);

    JVM::GetEnv()->DeleteGlobalRef(mCallbacks);
    vterm_free(mVt);

    delete mScroll;
}

status_t Terminal::run() {
    struct termios termios;
    memset(&termios, 0, sizeof(termios));
    termios.c_iflag = ICRNL|IXON|IUTF8;
    termios.c_oflag = OPOST|ONLCR|NL0|CR0|TAB0|BS0|VT0|FF0;
    termios.c_cflag = CS8|CREAD;
    termios.c_lflag = ISIG|ICANON|IEXTEN|ECHO|ECHOE|ECHOK;

    cfsetispeed(&termios, B38400);
    cfsetospeed(&termios, B38400);

    termios.c_cc[VINTR]    = 0x1f & 'C';
    termios.c_cc[VQUIT]    = 0x1f & '\\';
    termios.c_cc[VERASE]   = 0x7f;
    termios.c_cc[VKILL]    = 0x1f & 'U';
    termios.c_cc[VEOF]     = 0x1f & 'D';
    termios.c_cc[VSTART]   = 0x1f & 'Q';
    termios.c_cc[VSTOP]    = 0x1f & 'S';
    termios.c_cc[VSUSP]    = 0x1f & 'Z';
    termios.c_cc[VREPRINT] = 0x1f & 'R';
    termios.c_cc[VWERASE]  = 0x1f & 'W';
    termios.c_cc[VLNEXT]   = 0x1f & 'V';
    termios.c_cc[VMIN]     = 1;
    termios.c_cc[VTIME]    = 0;

    struct winsize size = { mRows, mCols, 0, 0 };

    int stderr_save_fd = dup(2);
    if (stderr_save_fd < 0) {
        ALOGE("failed to dup stderr - %s", strerror(errno));
    }

    mChildPid = forkpty(&mMasterFd, NULL, &termios, &size);
    if (mChildPid == 0) {
        /* Restore the ISIG signals back to defaults */
        signal(SIGINT, SIG_DFL);
        signal(SIGQUIT, SIG_DFL);
        signal(SIGSTOP, SIG_DFL);
        signal(SIGCONT, SIG_DFL);

        FILE *stderr_save = fdopen(stderr_save_fd, "a");

        if (!stderr_save) {
            ALOGE("failed to open stderr - %s", strerror(errno));
        }

        // We know execvp(2) won't actually try to modify this.
        char *shell = const_cast<char*>("/system/bin/sh");
#if USE_TEST_SHELL
        char *args[4] = {shell, "-c", "x=1; c=0; while true; do echo -e \"stop \e[00;3${c}mechoing\e[00m yourself! ($x)\"; x=$(( $x + 1 )); c=$((($c+1)%7)); if [ $x -gt 110 ]; then sleep 0.5; fi; done", NULL};
#else
        char *args[2] = {shell, NULL};
#endif

        execvp(shell, args);
        fprintf(stderr_save, "Cannot exec(%s) - %s\n", shell, strerror(errno));
        _exit(1);
    }

    ALOGD("entering read() loop");
    while (1) {
        char buffer[4096];
        ssize_t bytes = ::read(mMasterFd, buffer, sizeof buffer);
#if DEBUG_IO
        ALOGD("read() returned %d bytes", bytes);
#endif

        if (mKilled) {
            ALOGD("kill() requested");
            break;
        }
        if (bytes == 0) {
            ALOGD("read() found EOF");
            break;
        }
        if (bytes == -1) {
            ALOGE("read() failed: %s", strerror(errno));
            return 1;
        }

        {
            Mutex::Autolock lock(mLock);
            vterm_push_bytes(mVt, buffer, bytes);
            vterm_screen_flush_damage(mVts);
        }
    }

    return 0;
}

size_t Terminal::write(const char *bytes, size_t len) {
    return ::write(mMasterFd, bytes, len);
}

bool Terminal::dispatchCharacter(int mod, int character) {
    Mutex::Autolock lock(mLock);
    vterm_input_push_char(mVt, static_cast<VTermModifier>(mod), character);
    return flushInput();
}

bool Terminal::dispatchKey(int mod, int key) {
    Mutex::Autolock lock(mLock);
    vterm_input_push_key(mVt, static_cast<VTermModifier>(mod), static_cast<VTermKey>(key));
    return flushInput();
}

bool Terminal::flushInput() {
    size_t len = vterm_output_get_buffer_current(mVt);
    if (len) {
        char buf[len];
        len = vterm_output_bufferread(mVt, buf, len);
        return len == write(buf, len);
    }
    return true;
}

status_t Terminal::resize(dimen_t rows, dimen_t cols, dimen_t scrollRows) {
    Mutex::Autolock lock(mLock);

    ALOGD("resize(%d, %d, %d)", rows, cols, scrollRows);

    mRows = rows;
    mCols = cols;
    // TODO: resize scrollback

    struct winsize size = { rows, cols, 0, 0 };
    ioctl(mMasterFd, TIOCSWINSZ, &size);

    vterm_set_size(mVt, rows, cols);
    vterm_screen_flush_damage(mVts);

    return 0;
}

status_t Terminal::onPushline(dimen_t cols, const VTermScreenCell* cells) {
    ScrollbackLine* line = NULL;
    if (mScrollCur == mScrollSize) {
        /* Recycle old row if it's the right size */
        if (mScroll[mScrollCur - 1]->cols == cols) {
            line = mScroll[mScrollCur - 1];
        } else {
            delete mScroll[mScrollCur - 1];
        }

        memmove(mScroll + 1, mScroll, sizeof(ScrollbackLine*) * (mScrollCur - 1));
    } else if (mScrollCur > 0) {
        memmove(mScroll + 1, mScroll, sizeof(ScrollbackLine*) * mScrollCur);
    }

    if (line == NULL) {
        line = new ScrollbackLine(cols);
    }

    mScroll[0] = line;

    if (mScrollCur < mScrollSize) {
        mScrollCur++;
    }

    line->copyFrom(cols, cells);
    return 1;
}

status_t Terminal::onPopline(dimen_t cols, VTermScreenCell* cells) {
    if (mScrollCur == 0) {
        return 0;
    }

    ScrollbackLine* line = mScroll[0];
    mScrollCur--;
    memmove(mScroll, mScroll + 1, sizeof(ScrollbackLine*) * mScrollCur);

    dimen_t n = line->copyTo(cols, cells);
    for (dimen_t col = n; col < cols; col++) {
        cells[col].chars[0] = 0;
        cells[col].width = 1;
    }

    delete line;
    return 1;
}

void Terminal::getCellLocked(VTermPos pos, VTermScreenCell* cell) {
    // The UI may be asking for cell data while the model is changing
    // underneath it, so we always fill with meaningful data.

    if (pos.row < 0) {
        size_t scrollRow = -pos.row;
        if (scrollRow > mScrollCur) {
            // Invalid region above current scrollback
            cell->width = 1;
#if DEBUG_SCROLLBACK
            cell->bg.red = 255;
#endif
            return;
        }

        ScrollbackLine* line = mScroll[scrollRow - 1];
        if ((size_t) pos.col < line->cols) {
            // Valid scrollback cell
            line->getCell(pos.col, cell);
            cell->width = 1;
#if DEBUG_SCROLLBACK
            cell->bg.blue = 255;
#endif
            return;
        } else {
            // Extend last scrollback cell into invalid region
            line->getCell(line->cols - 1, cell);
            cell->width = 1;
            cell->chars[0] = ' ';
#if DEBUG_SCROLLBACK
            cell->bg.green = 255;
#endif
            return;
        }
    }

    if ((size_t) pos.row >= mRows) {
        // Invalid region below screen
        cell->width = 1;
#if DEBUG_SCROLLBACK
        cell->bg.red = 128;
#endif
        return;
    }

    // Valid screen cell
    vterm_screen_get_cell(mVts, pos, cell);
}

dimen_t Terminal::getRows() const {
    return mRows;
}

dimen_t Terminal::getCols() const {
    return mCols;
}

dimen_t Terminal::getScrollRows() const {
    return mScrollSize;
}

jobject Terminal::getCallbacks() const {
    return mCallbacks;
}

/*
 * JNI glue
 */

static jlong com_android_terminal_Terminal_nativeInit(JNIEnv* env, jclass clazz, jobject callbacks) {
    return reinterpret_cast<jlong>(new Terminal(callbacks));
}

static jint com_android_terminal_Terminal_nativeDestroy(JNIEnv* env, jclass clazz, jlong ptr) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    delete term;
    return 0;
}

static jint com_android_terminal_Terminal_nativeRun(JNIEnv* env, jclass clazz, jlong ptr) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->run();
}

static jint com_android_terminal_Terminal_nativeResize(JNIEnv* env,
        jclass clazz, jlong ptr, jint rows, jint cols, jint scrollRows) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->resize(rows, cols, scrollRows);
}

static inline int toArgb(const VTermColor& color) {
    return (0xff << 24 | color.red << 16 | color.green << 8 | color.blue);
}

static inline bool isCellStyleEqual(const VTermScreenCell& a, const VTermScreenCell& b) {
    if (toArgb(a.fg) != toArgb(b.fg)) return false;
    if (toArgb(a.bg) != toArgb(b.bg)) return false;

    if (a.attrs.bold != b.attrs.bold) return false;
    if (a.attrs.underline != b.attrs.underline) return false;
    if (a.attrs.italic != b.attrs.italic) return false;
    if (a.attrs.blink != b.attrs.blink) return false;
    if (a.attrs.reverse != b.attrs.reverse) return false;
    if (a.attrs.strike != b.attrs.strike) return false;
    if (a.attrs.font != b.attrs.font) return false;

    return true;
}

static jint com_android_terminal_Terminal_nativeGetCellRun(JNIEnv* env,
        jclass clazz, jlong ptr, jint row, jint col, jobject run) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    Mutex::Autolock lock(term->mLock);

    jcharArray dataArray = (jcharArray) env->GetObjectField(run, cellRunDataField);
    ScopedCharArrayRW data(env, dataArray);
    if (data.get() == NULL) {
        return -1;
    }

    VTermScreenCell firstCell, cell;

    VTermPos pos = {
        .row = row,
        .col = col,
    };

    size_t dataSize = 0;
    size_t colSize = 0;
    while ((size_t) pos.col < term->getCols()) {
        memset(&cell, 0, sizeof(VTermScreenCell));
        term->getCellLocked(pos, &cell);

        if (colSize == 0) {
            env->SetIntField(run, cellRunFgField, toArgb(cell.fg));
            env->SetIntField(run, cellRunBgField, toArgb(cell.bg));
            memcpy(&firstCell, &cell, sizeof(VTermScreenCell));
        } else {
            if (!isCellStyleEqual(cell, firstCell)) {
                break;
            }
        }

        // Only include cell chars if they fit into run
        uint32_t rawCell = cell.chars[0];
        size_t size = (rawCell < 0x10000) ? 1 : 2;
        if (dataSize + size <= data.size()) {
            if (rawCell < 0x10000) {
                data[dataSize++] = rawCell;
            } else {
                data[dataSize++] = (((rawCell - 0x10000) >> 10) & 0x3ff) + 0xd800;
                data[dataSize++] = ((rawCell - 0x10000) & 0x3ff) + 0xdc00;
            }

            for (int i = 1; i < cell.width; i++) {
                data[dataSize++] = ' ';
            }

            colSize += cell.width;
            pos.col += cell.width;
        } else {
            break;
        }
    }

    env->SetIntField(run, cellRunDataSizeField, dataSize);
    env->SetIntField(run, cellRunColSizeField, colSize);

    return 0;
}

static jint com_android_terminal_Terminal_nativeGetRows(JNIEnv* env, jclass clazz, jlong ptr) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->getRows();
}

static jint com_android_terminal_Terminal_nativeGetCols(JNIEnv* env, jclass clazz, jlong ptr) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->getCols();
}

static jint com_android_terminal_Terminal_nativeGetScrollRows(JNIEnv* env, jclass clazz, jlong ptr) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->getScrollRows();
}

static jboolean com_android_terminal_Terminal_nativeDispatchCharacter(JNIEnv *env, jclass clazz,
        jlong ptr, jint mod, jint c) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->dispatchCharacter(mod, c);
}

static jboolean com_android_terminal_Terminal_nativeDispatchKey(JNIEnv *env, jclass clazz,
        jlong ptr, jint mod, jint c) {
    Terminal* term = reinterpret_cast<Terminal*>(ptr);
    return term->dispatchKey(mod, c);
}

// https://gist.github.com/poutyface/1119105
int jniRegisterNativeMethods(JNIEnv *env, const char *className,
                             const JNINativeMethod *gMethods, int numMethods) {
    jclass klass;

    ALOGD("Registering %s natives\n", className);
    klass = env->FindClass(className);
    if(klass == NULL){
        ALOGE("Native registration unable to find class %s\n", className);
        return -1;
    }

    if(env->RegisterNatives(klass, gMethods, numMethods) < 0){
        ALOGE("RegisterNatives failed ofr %s\n", className);
        return -1;
    }

    return 0;
}

static JNINativeMethod gMethods[] = {
    { "nativeInit", "(La/o/s/p/terminal/TerminalCallbacks;)J", (void*)com_android_terminal_Terminal_nativeInit },
    { "nativeDestroy", "(J)I", (void*)com_android_terminal_Terminal_nativeDestroy },
    { "nativeRun", "(J)I", (void*)com_android_terminal_Terminal_nativeRun },
    { "nativeResize", "(JIII)I", (void*)com_android_terminal_Terminal_nativeResize },
    { "nativeGetCellRun", "(JIILa/o/s/p/terminal/Terminal$CellRun;)I", (void*)com_android_terminal_Terminal_nativeGetCellRun },
    { "nativeGetRows", "(J)I", (void*)com_android_terminal_Terminal_nativeGetRows },
    { "nativeGetCols", "(J)I", (void*)com_android_terminal_Terminal_nativeGetCols },
    { "nativeGetScrollRows", "(J)I", (void*)com_android_terminal_Terminal_nativeGetScrollRows },
    { "nativeDispatchCharacter", "(JII)Z", (void*)com_android_terminal_Terminal_nativeDispatchCharacter},
    { "nativeDispatchKey", "(JII)Z", (void*)com_android_terminal_Terminal_nativeDispatchKey },
};

int register_com_android_terminal_Terminal() {
    JNIEnv* env = JVM::GetEnv();
    ScopedLocalRef<jclass> localClass(env,
            env->FindClass((const char *) "a/o/s/p/terminal/TerminalCallbacks"));

    android::terminalCallbacksClass = reinterpret_cast<jclass>(env->NewGlobalRef(localClass.get()));

    android::damageMethod = env->GetMethodID(terminalCallbacksClass, "damage", "(IIII)I");
    android::moveRectMethod = env->GetMethodID(terminalCallbacksClass, "moveRect", "(IIIIIIII)I");
    android::moveCursorMethod = env->GetMethodID(terminalCallbacksClass, "moveCursor",
            "(IIIII)I");
    android::setTermPropBooleanMethod = env->GetMethodID(terminalCallbacksClass,
            "setTermPropBoolean", "(IZ)I");
    android::setTermPropIntMethod = env->GetMethodID(terminalCallbacksClass, "setTermPropInt",
            "(II)I");
    android::setTermPropStringMethod = env->GetMethodID(terminalCallbacksClass, "setTermPropString",
            "(ILjava/lang/String;)I");
    android::setTermPropColorMethod = env->GetMethodID(terminalCallbacksClass, "setTermPropColor",
            "(IIII)I");
    android::bellMethod = env->GetMethodID(terminalCallbacksClass, "bell", "()I");

    ScopedLocalRef<jclass> cellRunLocal(env,
            env->FindClass((const char *) "a/o/s/p/terminal/Terminal$CellRun"));
    cellRunClass = reinterpret_cast<jclass>(env->NewGlobalRef(cellRunLocal.get()));
    cellRunDataField = env->GetFieldID(cellRunClass, "data", "[C");
    cellRunDataSizeField = env->GetFieldID(cellRunClass, "dataSize", "I");
    cellRunColSizeField = env->GetFieldID(cellRunClass, "colSize", "I");
    cellRunFgField = env->GetFieldID(cellRunClass, "fg", "I");
    cellRunBgField = env->GetFieldID(cellRunClass, "bg", "I");

    return jniRegisterNativeMethods(env, "a/o/s/p/terminal/Terminal",
            gMethods, NELEM(gMethods));
}

} /* namespace android */
