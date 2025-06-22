package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.text.Spannable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProviderInfoViewModel(application: Application, private val providerInfoModel: ProviderInfoModel) : AndroidViewModel(application) {

    private val providerInfo: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            loadData()
        }
    }

    fun getProviderInfo(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return providerInfo
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            providerInfo.postValue(arrayListOf(
                    getProviderName(),
                    getProviderPath(),
                    getFlags(),
                    getAuthority(),
                    getInitOrder()
            ))
        }
    }

    private fun getProviderName(): Pair<Int, Spannable> {
        return Pair(R.string.name,
                    providerInfoModel.providerInfo.name.applySecondaryTextColor())
    }

    private fun getProviderPath(): Pair<Int, Spannable> {
        return Pair(R.string.path,
                    buildString {
                        append(providerInfoModel.providerInfo.applicationInfo.packageName)
                        append("/")
                        append(providerInfoModel.providerInfo.name)
                    }.applySecondaryTextColor())
    }

    private fun getFlags(): Pair<Int, Spannable> {
        return Pair(R.string.flags,
                    MetaUtils.getFlags(providerInfoModel.providerInfo.flags, getApplication()).applyAccentColor())
    }

    private fun getAuthority(): Pair<Int, Spannable> {
        return try {
            Pair(R.string.authority,
                 providerInfoModel.authority.applySecondaryTextColor())
        } catch (e: NullPointerException) {
            Pair(R.string.authority,
                 getApplication<Application>().getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getInitOrder(): Pair<Int, Spannable> {
        return Pair(R.string.init_order,
                    providerInfoModel.providerInfo.initOrder.toString().applySecondaryTextColor())
    }
}