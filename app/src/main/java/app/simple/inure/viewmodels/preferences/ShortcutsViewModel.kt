package app.simple.inure.viewmodels.preferences

import android.app.Application
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
import app.simple.inure.constants.ShortcutConstants.MOST_USED_ACTION
import app.simple.inure.constants.ShortcutConstants.MOST_USED_ID
import app.simple.inure.constants.ShortcutConstants.NOTES_ACTION
import app.simple.inure.constants.ShortcutConstants.NOTES_ID
import app.simple.inure.constants.ShortcutConstants.RECENTLY_INSTALLED_ACTION
import app.simple.inure.constants.ShortcutConstants.RECENTLY_INSTALLED_ID
import app.simple.inure.constants.ShortcutConstants.RECENTLY_UPDATED_ACTION
import app.simple.inure.constants.ShortcutConstants.RECENTLY_UPDATED_ID
import app.simple.inure.constants.ShortcutConstants.TERMINAL_ACTION
import app.simple.inure.constants.ShortcutConstants.TERMINAL_ID
import app.simple.inure.constants.ShortcutConstants.UNINSTALLED_ACTION
import app.simple.inure.constants.ShortcutConstants.UNINSTALLED_ID
import app.simple.inure.constants.ShortcutConstants.USAGE_STATS_ACTION
import app.simple.inure.constants.ShortcutConstants.USAGE_STATS_ID
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.ShortcutModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShortcutsViewModel(application: Application) : WrappedViewModel(application) {

    private val shortcuts: MutableLiveData<List<ShortcutModel>> by lazy {
        MutableLiveData<List<ShortcutModel>>().also {
            loadShortcuts()
        }
    }

    fun getShortcuts(): LiveData<List<ShortcutModel>> {
        return shortcuts
    }

    private fun loadShortcuts() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = context

            val list = arrayListOf(
                    ShortcutModel(R.drawable.sc_apps, APPS_ID, APPS_ACTION, context.getString(R.string.apps)),
                    ShortcutModel(R.drawable.sc_terminal, TERMINAL_ID, TERMINAL_ACTION, context.getString(R.string.terminal)),
                    ShortcutModel(R.drawable.sc_batch, BATCH_ID, BATCH_ACTION, context.getString(R.string.batch)),
                    ShortcutModel(R.drawable.sc_stats, USAGE_STATS_ID, USAGE_STATS_ACTION, context.getString(R.string.usage_statistics)),
                    ShortcutModel(R.drawable.sc_analytics, ANALYTICS_ID, ANALYTICS_ACTION, context.getString(R.string.analytics)),
                    ShortcutModel(R.drawable.sc_notes, NOTES_ID, NOTES_ACTION, context.getString(R.string.notes)),
                    ShortcutModel(R.drawable.sc_recently_installed, RECENTLY_INSTALLED_ID, RECENTLY_INSTALLED_ACTION, context.getString(R.string.recently_installed)),
                    ShortcutModel(R.drawable.sc_recently_updated, RECENTLY_UPDATED_ID, RECENTLY_UPDATED_ACTION, context.getString(R.string.recently_updated)),
                    ShortcutModel(R.drawable.sc_most_used, MOST_USED_ID, MOST_USED_ACTION, context.getString(R.string.most_used)),
                    ShortcutModel(R.drawable.sc_uninstalled, UNINSTALLED_ID, UNINSTALLED_ACTION, context.getString(R.string.uninstalled)),
            )

            list.sortBy {
                it.name
            }

            shortcuts.postValue(list)
        }
    }
}