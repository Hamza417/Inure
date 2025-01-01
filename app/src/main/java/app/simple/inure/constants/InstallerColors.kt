package app.simple.inure.constants

object InstallerColors {

    const val UNKNOWN = 0xFF6D7291.toInt()

    private val colors: ArrayList<Int> by lazy {
        arrayListOf(
                0xFFEA4335.toInt(), // Play Store
                0xFFFFB401.toInt(), // Aurora Store
                0xFF217AD3.toInt(), // F-Droid
                0xFFFFA967.toInt(), // Inure
                0xFF4CAF50.toInt(), // APKMirror
                0xFF9E9E9E.toInt(), // System
                0xFF673AB7.toInt(), // Aptoide
                0xFF00C4F7.toInt(), // Amazon Appstore
                0xFF2196F3.toInt(), // APKPure
                0xFF3F51B5.toInt(), // Samsung Galaxy Store
                0xFFF65C5B.toInt(), // Huawei AppGallery
                0xFFE91E63.toInt(), // SlideME
        )
    }

    private val colorMap = mapOf(
            "com.android.vending" to 0xFFEA4335.toInt(),
            "com.google.android.packageinstaller" to 0xFFEA4335.toInt(),
            "com.aurora.store" to 0xFFFFB401.toInt(),
            "org.fdroid.fdroid" to 0xFF217AD3.toInt(),
            "app.simple.inure" to 0xFFFFA967.toInt(),
            "com.apkmirror" to 0xFF4CAF50.toInt(),
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
    )

    fun getInstallerColors(): ArrayList<Int> {
        return colors
    }

    fun getInstallerColorMap(): Map<String, Int> {
        return colorMap
    }
}