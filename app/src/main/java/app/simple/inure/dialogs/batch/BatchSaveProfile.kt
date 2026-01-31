package app.simple.inure.dialogs.batch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.database.instances.BatchProfileDatabase
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.models.BatchProfile
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.util.TextViewUtils.doOnTextChanged
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatchSaveProfile : ScopedDialogFragment() {

    private lateinit var packages: ArrayList<String>
    private lateinit var profileNames: ArrayList<String>

    private lateinit var editText: DynamicCornerEditText
    private lateinit var save: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_save_batch_profile, container, false)

        packages = requireArguments().getStringArrayList(BundleConstants.PACKAGES)!!
        editText = view.findViewById(R.id.edit_text)
        save = view.findViewById(R.id.save)
        close = view.findViewById(R.id.close)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editText.showInput()
        save.gone()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                BatchProfileDatabase.getInstance(requireContext())?.batchProfileDao().let { dao ->
                    profileNames = dao?.getBatchProfiles()?.map { it.profileName } as ArrayList<String>
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    showWarning(it.message.toString())
                }
            }
        }

        editText.doOnTextChanged { text, _, _, _ ->
            if (text!!.isNotEmpty()) {
                if (profileNames.contains(text.toString())) {
                    editText.error = "Profile name already exists"
                    save.gone(animate = true)
                } else {
                    editText.error = null
                    save.visible(animate = true)
                }
            } else {
                save.gone(animate = true)
            }
        }

        save.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                kotlin.runCatching {
                    BatchProfileDatabase.getInstance(requireContext())?.batchProfileDao().let { dao ->
                        packages.joinToString(separator = ",", prefix = "", postfix = "").let { joinedPackages ->
                            dao?.insertBatchProfile(
                                    BatchProfile(editText.text.toString(),
                                                 joinedPackages,
                                                 BatchPreferences.getAppsFilter(),
                                                 BatchPreferences.getSortStyle(),
                                                 BatchPreferences.isReverseSorting(),
                                                 BatchPreferences.getAppsCategory(),
                                                 System.currentTimeMillis()))
                        }

                        BatchPreferences.setLastSelectedProfile(dao?.getIdFromName(editText.text.toString())!!)

                        withContext(Dispatchers.Main) {
                            dismiss()
                        }
                    }
                }.getOrElse {
                    withContext(Dispatchers.Main) {
                        showWarning(it.message.toString())
                    }
                }
            }
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(packages: ArrayList<String>): BatchSaveProfile {
            val args = Bundle()
            args.putStringArrayList(BundleConstants.PACKAGES, packages)
            val fragment = BatchSaveProfile()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showBatchProfileSave(packages: ArrayList<String>): BatchSaveProfile {
            val dialog = newInstance(packages)
            dialog.show(this, "batch_profile_save")
            return dialog
        }
    }
}