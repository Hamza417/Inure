package app.simple.inure.constants

object TerminalConstants {
    private val commandHints = arrayOf(
            "fstrim -v /data",
            "fstrim -v /cache",
            "fstrim -v /system",
            "fstrim -v /vendor",
            "input keyevent 26",
            "input keyevent 82",
            "input keyevent 3",
            "input keyevent 4",
            "input keyevent 187",
            "echo 3 > /proc/sys/vm/drop_caches",
            "echo 0 > /proc/sys/vm/drop_caches",
            "echo 1 > /proc/sys/vm/drop_caches",
            "echo 2 > /proc/sys/vm/drop_caches",
            "touch /cache/recovery/command",
            "echo --update_package=/sdcard/Download/update.zip > /cache/recovery/command",
            "echo --update_package=/sdcard/Download/update.zip > /cache/recovery/last_install",
            "echo --wipe_data > /cache/recovery/command",
            "echo --wipe_cache > /cache/recovery/command",
            "echo --wipe_data > /cache/recovery/last_install",
            "echo --wipe_cache > /cache/recovery/last_install",
            "echo --wipe_data > /cache/recovery/command",
            "rm -rf /cache/recovery/command",
            "reboot recovery",
            "reboot bootloader",
            "reboot",
            "su -c reboot recovery",
            "su -c reboot bootloader",
            "su -c reboot",
            "su -c reboot recovery",
            "cd /data/data/app.simple.inure/app_HOME",
            "cd \$HOME",
            "busybox mount -o remount,rw /",
            "busybox mount -o remount,ro /",
            "busybox mount -o remount,rw /data",
            "busybox mount -o remount,ro /data",
            "busybox mount -o remount,rw /cache",
            "busybox mount -o remount,ro /cache",
            "busybox mount -o remount,rw /vendor",
            "busybox mount -o remount,ro /vendor",
            "busybox mount -o remount,rw /system",
            "busybox mount -o remount,ro /system",
    )

    fun getRandomCommandHint(): String {
        return commandHints.random()
    }
}