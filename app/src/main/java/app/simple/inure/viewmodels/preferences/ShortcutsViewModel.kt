package app.simple.inure.viewmodels.preferences

import android.app.Application
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.constants.ShortcutConstants.ANALYTICS_ACTION
import app.simple.inure.constants.ShortcutConstants.ANALYTICS_ID
import app.simple.inure.constants.ShortcutConstants.APPS_ACTION
import app.simple.inure.constants.ShortcutConstants.APPS_ID
import app.simple.inure.constants.ShortcutConstants.BATCH_ACTION
import app.simple.inure.constants.ShortcutConstants.BATCH_ID
import app.simple.inure.constants.ShortcutConstants.DEBLOAT_ACTION
import app.simple.inure.constants.ShortcutConstants.DEBLOAT_ID
import app.simple.inure.constants.ShortcutConstants.FOSS_ACTION
import app.simple.inure.constants.ShortcutConstants.FOSS_ID
import app.simple.inure.constants.ShortcutConstants.MOST_USED_ACTION
import app.simple.inure.constants.ShortcutConstants.MOST_USED_ID
import app.simple.inure.constants.ShortcutConstants.MUSIC_ACTION
import app.simple.inure.constants.ShortcutConstants.MUSIC_ID
import app.simple.inure.constants.ShortcutConstants.NOTES_ACTION
import app.simple.inure.constants.ShortcutConstants.NOTES_ID
import app.simple.inure.constants.ShortcutConstants.PREFERENCES_ACTION
import app.simple.inure.constants.ShortcutConstants.PREFERENCES_ID
import app.simple.inure.constants.ShortcutConstants.RECENTLY_INSTALLED_ACTION
import app.simple.inure.constants.ShortcutConstants.RECENTLY_INSTALLED_ID
import app.simple.inure.constants.ShortcutConstants.RECENTLY_UPDATED_ACTION
import app.simple.inure.constants.ShortcutConstants.RECENTLY_UPDATED_ID
import app.simple.inure.constants.ShortcutConstants.SEARCH_ACTION
import app.simple.inure.constants.ShortcutConstants.SEARCH_ID
import app.simple.inure.constants.ShortcutConstants.TAGS_ACTION
import app.simple.inure.constants.ShortcutConstants.TAGS_ID
import app.simple.inure.constants.ShortcutConstants.TERMINAL_ACTION
import app.simple.inure.constants.ShortcutConstants.TERMINAL_ID
import app.simple.inure.constants.ShortcutConstants.UNINSTALLED_ACTION
import app.simple.inure.constants.ShortcutConstants.UNINSTALLED_ID
import app.simple.inure.constants.ShortcutConstants.USAGE_STATS_ACTION
import app.simple.inure.constants.ShortcutConstants.USAGE_STATS_ID
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Shortcut
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShortcutsViewModel(application: Application) : WrappedViewModel(application) {

    private val shortcuts: MutableLiveData<List<Shortcut>> by lazy {
        MutableLiveData<List<Shortcut>>().also {
            loadShortcuts()
        }
    }

    fun getShortcuts(): LiveData<List<Shortcut>> {
        return shortcuts
    }

    private fun loadShortcuts() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf(
                    Shortcut(R.drawable.sc_apps, APPS_ID, APPS_ACTION, R.string.apps),
                    Shortcut(R.drawable.sc_terminal, TERMINAL_ID, TERMINAL_ACTION, R.string.terminal),
                    Shortcut(R.drawable.sc_batch, BATCH_ID, BATCH_ACTION, R.string.batch),
                    Shortcut(R.drawable.sc_stats, USAGE_STATS_ID, USAGE_STATS_ACTION, R.string.usage_statistics),
                    Shortcut(R.drawable.sc_analytics, ANALYTICS_ID, ANALYTICS_ACTION, R.string.analytics),
                    Shortcut(R.drawable.sc_notes, NOTES_ID, NOTES_ACTION, R.string.notes),
                    Shortcut(R.drawable.sc_recently_installed, RECENTLY_INSTALLED_ID, RECENTLY_INSTALLED_ACTION, R.string.recently_installed),
                    Shortcut(R.drawable.sc_recently_updated, RECENTLY_UPDATED_ID, RECENTLY_UPDATED_ACTION, R.string.recently_updated),
                    Shortcut(R.drawable.sc_most_used, MOST_USED_ID, MOST_USED_ACTION, R.string.most_used),
                    Shortcut(R.drawable.sc_uninstalled, UNINSTALLED_ID, UNINSTALLED_ACTION, R.string.uninstalled),
                    Shortcut(R.drawable.sc_preferences, PREFERENCES_ID, PREFERENCES_ACTION, R.string.preferences),
                    Shortcut(R.drawable.sc_search, SEARCH_ID, SEARCH_ACTION, R.string.search),
                    Shortcut(R.drawable.sc_tags, TAGS_ID, TAGS_ACTION, R.string.tags),
                    Shortcut(R.drawable.sc_open_source, FOSS_ID, FOSS_ACTION, R.string.foss),
            )

            if (DevelopmentPreferences.get(DevelopmentPreferences.music)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    list.add(Shortcut(R.drawable.sc_music, MUSIC_ID, MUSIC_ACTION, R.string.music))
                }
            }

            if (AppUtils.isGithubFlavor() || AppUtils.isBetaFlavor()) {
                Shortcut(R.drawable.sc_recycling, DEBLOAT_ID, DEBLOAT_ACTION, R.string.debloat).also {
                    list.add(it)
                }
            }

            list.sortBy {
                it.name
            }

            shortcuts.postValue(list)
        }
    }
}
