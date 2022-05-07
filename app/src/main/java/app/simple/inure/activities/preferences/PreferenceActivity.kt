package app.simple.inure.activities.preferences

import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.theme.ThemeCoordinatorLayout
import app.simple.inure.extension.activities.BaseActivity
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.ui.panels.Preferences
import app.simple.inure.util.NullSafety.isNull

class PreferenceActivity : BaseActivity(), ThemeChangedListener {

    private lateinit var circularRevealImageView: ImageView
    private lateinit var container: ThemeCoordinatorLayout
    private lateinit var content: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager.addListener(this)

        circularRevealImageView = findViewById(R.id.theme_reveal)
        container = findViewById(R.id.app_container)
        content = findViewById(android.R.id.content)

        content.setBackgroundColor(ThemeManager.theme.viewGroupTheme.background)

        if (savedInstanceState.isNull()) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.app_container, Preferences.newInstance(), "preferences")
                .commit()
        }
    }
}