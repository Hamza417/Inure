package app.simple.inure.dialogs.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.installer.AdapterUsers
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.User
import app.simple.inure.viewmodels.dialogs.UsersViewModel

class Users : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private val usersViewModel: UsersViewModel by viewModels()
    private var adapterUsers: AdapterUsers? = null
    private var usersCallback: UsersCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_users, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        usersViewModel.getUsers().observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                adapterUsers = AdapterUsers(it) {
                    usersCallback?.onUserSelected(it)
                    dismiss()
                }

                recyclerView.adapter = adapterUsers
            }
        }
    }

    fun setUsersCallback(usersCallback: UsersCallback) {
        this.usersCallback = usersCallback
    }

    companion object {
        fun newInstance(): Users {
            val args = Bundle()
            val fragment = Users()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUsers(): Users {
            val dialog = newInstance()
            dialog.show(this, "users")
            return dialog
        }

        interface UsersCallback {
            fun onUserSelected(user: User)
        }
    }
}