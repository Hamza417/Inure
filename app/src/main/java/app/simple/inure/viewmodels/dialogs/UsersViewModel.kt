package app.simple.inure.viewmodels.dialogs

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.User
import app.simple.inure.shizuku.ShizukuUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsersViewModel(application: Application) : RootShizukuViewModel(application) {

    private val users: MutableLiveData<ArrayList<User>> by lazy {
        MutableLiveData<ArrayList<User>>().also {
            initializeCoreFramework()
        }
    }

    fun getUsers(): LiveData<ArrayList<User>> {
        return users
    }

    override fun onShellCreated(shell: Shell?) {
        super.onShellCreated(shell)
        loadUsersRoot()
    }

    override fun onShizukuCreated() {
        super.onShizukuCreated()
        loadUsersShizuku()
    }

    private fun loadUsersRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            /**
             * UserInfo{0:Hamza Rizwan:c13}
             *
             * 0: User ID
             * Hamza Rizwan: User Name
             * c13: hexFlags
             */
            kotlin.runCatching {
                val users = ArrayList<User>()

                Shell.cmd("pm list users").exec().let {
                    if (it.isSuccess) {
                        it.out.forEach { line ->
                            if (line.contains("UserInfo") && line.contains("{") && line.contains("}")) {
                                val split = line.substringAfter("{").substringBefore("}").split(":")
                                users.add(User(split[0].toInt(), split[1], split[2]))
                            }
                        }

                        users.sortBy { user -> user.id }
                        this@UsersViewModel.users.postValue(users)
                    } else {
                        postWarning(it.err.toString())
                    }
                }
            }.onFailure {
                postError(it)
            }
        }
    }

    private fun loadUsersShizuku() {
        viewModelScope.launch(Dispatchers.Default) {
            runCatching {
                ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command("pm list users"), null).let {
                    if (it.isSuccess) {
                        val users = ArrayList<User>()
                        it.out.split("\n").forEach { line ->
                            if (line.contains("UserInfo") && line.contains("{") && line.contains("}")) {
                                val split = line.substringAfter("{").substringBefore("}").split(":")
                                users.add(User(split[0].toInt(), split[1], split[2]))
                            }
                        }

                        users.sortBy { user -> user.id }
                        this@UsersViewModel.users.postValue(users)
                    } else {
                        postWarning(it.err.toString())
                    }
                }
            }
        }
    }
}