package app.simple.inure.util

import com.topjohnwu.superuser.Shell

object SystemUtils {

    fun hasBusyBox(): Boolean {
        return Shell.rootAccess() && Shell.su("busybox").exec().isSuccess
    }
}