package app.simple.inure.dialogs.batch

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.dialogs.AdapterBatchProfiles
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.models.BatchProfile
import app.simple.inure.popups.batch.PopupBatchProfileMenu
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.viewmodels.dialogs.BatchProfilesViewModel

class BatchProfiles : ScopedBottomSheetFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var batchProfilesViewModel: BatchProfilesViewModel

    private var batchProfilesCallback: BatchProfilesCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_batch_profiles, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)
        batchProfilesViewModel = ViewModelProvider(this)[BatchProfilesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        batchProfilesViewModel.getProfiles().observe(viewLifecycleOwner) { profiles ->
            recyclerView.adapter = AdapterBatchProfiles(
                    profiles, object : AdapterBatchProfiles.Companion.AdapterBatchProfilesCallback {
                override fun onProfileLongClicked(profile: BatchProfile, view: View, position: Int) {
                    PopupBatchProfileMenu(view)
                        .setCallbacks(object : PopupBatchProfileMenu.Companion.BatchProfileMenuCallbacks {
                            override fun onSelect() {
                                batchProfilesCallback?.onProfileSelected(profile)
                            }

                            override fun onDelete() {
                                batchProfilesViewModel.deleteProfile(profile) {
                                    (recyclerView.adapter as AdapterBatchProfiles).removeProfile(profile)
                                }
                            }
                        })
                }

                override fun onProfileSelected(profile: BatchProfile) {
                    batchProfilesCallback?.onProfileSelected(profile)
                    dismiss()
                }
            })
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            BatchPreferences.lastSelectedProfile -> {
                dismiss()
            }
        }
    }

    fun setOnProfileSelected(batchProfilesCallback: BatchProfilesCallback) {
        this.batchProfilesCallback = batchProfilesCallback
    }

    companion object {
        fun newInstance(): BatchProfiles {
            val args = Bundle()
            val fragment = BatchProfiles()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchProfiles(): BatchProfiles {
            val dialog = newInstance()
            dialog.show(this, "batch_profiles")
            return dialog
        }

        interface BatchProfilesCallback {
            fun onProfileSelected(profile: BatchProfile)
        }
    }
}