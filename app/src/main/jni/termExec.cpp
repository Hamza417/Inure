#include "common.h"

#define LOG_TAG "Exec"

#include <sys/types.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <cerrno>
#include <fcntl.h>
#include <cstdlib>
#include <unistd.h>
#include <termios.h>
#include <csignal>

#include "termExec.h"

static void android_os_Exec_setPtyWindowSize(JNIEnv *env, jobject clazz,
                                             jint fd, jint row, jint col, jint xpixel,
                                             jint ypixel) {
    struct winsize sz{};

    sz.ws_row = row;
    sz.ws_col = col;
    sz.ws_xpixel = xpixel;
    sz.ws_ypixel = ypixel;

    // TODO: handle the situation, when the file descriptor is incompatible with TIOCSWINSZ (e.g. not from /dev/ptmx)
    if (ioctl(fd, TIOCSWINSZ, &sz) == -1)
        env->ThrowNew(env->FindClass("java/io/IOException"), "Failed to issue TIOCSWINSZ ioctl");
}

// tcgetattr /tcsetattr are not part of Bionic at API level 4. Here's a compatible version.

static __inline__ int my_tcgetattr(int fd, struct termios *s) {
    return ioctl(fd, TCGETS, s);
}

static __inline__ int my_tcsetattr(int fd, const struct termios *s) {
    return ioctl(fd, TCSETS, (void *) s);
}

static void android_os_Exec_setPtyUTF8Mode(JNIEnv *env, jobject clazz, jint fd, jboolean utf8Mode) {
    struct termios tios{};

    if (my_tcgetattr(fd, &tios) != 0)
        env->ThrowNew(env->FindClass("java/io/IOException"), "Failed to get terminal attributes");

    if (utf8Mode) {
        tios.c_iflag |= IUTF8;
    } else {
        tios.c_iflag &= ~IUTF8;
    }

    if (my_tcsetattr(fd, &tios) != 0)
        env->ThrowNew(env->FindClass("java/io/IOException"), "Failed to set terminal UTF-8 mode");
}

static const char *classPathName = "app/simple/inure/terminal/Exec";
static JNINativeMethod method_table[] = {
        {"setPtyWindowSizeInternal", "(IIIII)V",
                (void *) android_os_Exec_setPtyWindowSize},
        {"setPtyUTF8ModeInternal",   "(IZ)V",
                (void *) android_os_Exec_setPtyUTF8Mode}
};

int init_Exec(JNIEnv *env) {
    if (!registerNativeMethods(env, classPathName, method_table,
                               sizeof(method_table) / sizeof(method_table[0]))) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}
