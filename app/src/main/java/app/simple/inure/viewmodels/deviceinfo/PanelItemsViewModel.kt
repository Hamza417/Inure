package app.simple.inure.viewmodels.deviceinfo

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.BatteryUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PanelItemsViewModel(application: Application) : WrappedViewModel(application) {

    private val panelItems: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadItems()
        }
    }

    fun getPanelItems(): LiveData<List<Pair<Int, String>>> {
        return panelItems
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

            val list = listOf(
                    Pair(R.drawable.ic_android, context.getString(R.string.system)),
                    Pair(R.drawable.ic_phone, context.getString(R.string.device)),
                    Pair(R.drawable.ic_wifi, context.getString(R.string.hardware)),
                    Pair(R.drawable.ic_sd_storage, context.getString(R.string.memory)),
                    Pair(R.drawable.ic_camera, context.getString(R.string.camera)),
                    Pair(R.drawable.ic_network, context.getString(R.string.network)),
                    Pair(BatteryUtils.getBatteryDrawable(applicationContext()), context.getString(R.string.battery)),
                    Pair(R.drawable.ic_android, context.getString(R.string.media)),
            )

            panelItems.postValue(list)
        }
    }
}