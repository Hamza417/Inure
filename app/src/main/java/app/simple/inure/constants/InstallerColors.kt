package app.simple.inure.constants

/**
 * If you are developer who maintains apps that installs apps and acts as a
 * source for them as well, you can add your signature colors here. Or open an
 * issue for that.
 */
object InstallerColors {

    private val colorMap = mapOf(
            "com.android.vending" to 0xFFEA4335.toInt(),
            "com.google.android.packageinstaller" to 0xFFEA4335.toInt(),
            "com.aurora.store" to 0xFFFFB401.toInt(),
            "org.fdroid.fdroid" to 0xFF217AD3.toInt(),
            "app.simple.inure" to 0xFFFFA967.toInt(),
            "app.simple.inure.debug" to 0xFFFFA967.toInt(),
            "app.simple.inure.beta" to 0xFFFFA967.toInt(),
            "app.simple.inure.play" to 0xFFFFA967.toInt(),
            "com.apkmirror" to 0xFF4CAF50.toInt(),
            "android" to 0xFF9E9E9E.toInt(),
            "system" to 0xFF9E9E9E.toInt(),
            "com.android.shell" to 0xFF9E9E9E.toInt(),
            "com.aptoide.pt" to 0xFF673AB7.toInt(),
            "com.amazon.venezia" to 0xFF00C4F7.toInt(),
            "com.apkpure.aegon" to 0xFF2196F3.toInt(),
            "com.sec.android.app.samsungapps" to 0xFF3F51B5.toInt(),
            "com.huawei.appmarket" to 0xFFF65C5B.toInt(),
            "com.slideme.sam.manager" to 0xFFE91E63.toInt(),
            "com.xiaomi.market" to 0xFFE173E5.toInt(),
            "com.xiaomi.discover" to 0xFF3B56E5.toInt(),
            "com.uptodown" to 0xFFE5E53B.toInt(),
            "com.getjar.rewards" to 0xFF3BE5E5.toInt(),
            "com.opera.app.discover" to 0xFF3BE53B.toInt(),
            "com.opera.app.market" to 0xFFE53B3B.toInt(),
    )

    fun getInstallerColorMap(): Map<String, Int> {
        return colorMap
    }
}