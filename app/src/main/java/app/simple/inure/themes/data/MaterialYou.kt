package app.simple.inure.themes.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

object MaterialYou {

    val materialYouAccentResID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        android.R.color.system_accent1_500
    } else {
        -1
    }

    var headingTextColor = 0
    var primaryTextColor = 0
    var secondaryTextColor = 0
    var tertiaryTextColor = 0
    var quaternaryTextColor = 0

    var background = 0
    var viewerBackground = 0
    var highlightBackground = 0
    var selectedBackground = 0
    var dividerBackground = 0

    var switchOffColor = 0

    var regularIconColor = 0
    var secondaryIconColor = 0

    @RequiresApi(Build.VERSION_CODES.S)
    fun Context.presetMaterialYouDynamicColors() {
        headingTextColor = ContextCompat.getColor(this, android.R.color.system_neutral1_1000)
        primaryTextColor = ContextCompat.getColor(this, android.R.color.system_neutral1_900)
        secondaryTextColor = ContextCompat.getColor(this, android.R.color.system_neutral1_700)
        tertiaryTextColor = ContextCompat.getColor(this, android.R.color.system_neutral1_500)
        quaternaryTextColor = ContextCompat.getColor(this, android.R.color.system_neutral1_300)

        background = ContextCompat.getColor(this, android.R.color.system_neutral1_50)
        viewerBackground = ContextCompat.getColor(this, android.R.color.system_neutral1_50)
        highlightBackground = ContextCompat.getColor(this, android.R.color.system_accent1_300)
        selectedBackground = ContextCompat.getColor(this, android.R.color.system_accent2_100)
        dividerBackground = ContextCompat.getColor(this, android.R.color.system_accent3_900)

        switchOffColor = ContextCompat.getColor(this, android.R.color.system_neutral1_100)

        regularIconColor = ContextCompat.getColor(this, android.R.color.system_neutral1_900)
        secondaryIconColor = ContextCompat.getColor(this, android.R.color.system_neutral1_400)
    }
}
