package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.TerminalCommandDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.TerminalCommand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TerminalCommandsViewModel(application: Application) : WrappedViewModel(application) {

    private val terminalCommands: MutableLiveData<ArrayList<TerminalCommand>> by lazy {
        MutableLiveData<ArrayList<TerminalCommand>>().also {
            loadTerminalCommands()
        }
    }

    fun getTerminalCommands(): LiveData<ArrayList<TerminalCommand>> {
        return terminalCommands
    }

    private fun loadTerminalCommands() {
        viewModelScope.launch(Dispatchers.IO) {
            terminalCommands.postValue(TerminalCommandDatabase
                                           .getInstance(applicationContext())!!
                                           .terminalCommandDao()!!
                                           .getAllTerminalCommands() as ArrayList<TerminalCommand>?)
        }
    }

    fun deleteAll() {
        viewModelScope.launch(Dispatchers.IO) {
            TerminalCommandDatabase
                .getInstance(applicationContext())!!
                .terminalCommandDao()!!
                .nukeTable()
            terminalCommands.postValue(arrayListOf())
        }
    }

    fun addNewCommands(terminalCommand: TerminalCommand) {
        viewModelScope.launch(Dispatchers.IO) {
            TerminalCommandDatabase
                .getInstance(applicationContext())!!
                .terminalCommandDao()!!
                .insertTerminalCommand(terminalCommand)
            terminalCommands.postValue(TerminalCommandDatabase
                                           .getInstance(applicationContext())!!
                                           .terminalCommandDao()!!
                                           .getAllTerminalCommands() as ArrayList<TerminalCommand>?)
        }
    }

    fun deleteCommand(terminalCommand: TerminalCommand?) {
        viewModelScope.launch(Dispatchers.IO) {
            TerminalCommandDatabase
                .getInstance(applicationContext())!!
                .terminalCommandDao()!!
                .deleteTerminalCommand(terminalCommand!!)

            terminalCommands.value?.remove(terminalCommand)
        }
    }

    fun updateCommand(terminalCommand: TerminalCommand) {
        viewModelScope.launch(Dispatchers.IO) {
            TerminalCommandDatabase
                .getInstance(applicationContext())!!
                .terminalCommandDao()!!
                .updateTerminalCommand(terminalCommand)
        }
    }
}