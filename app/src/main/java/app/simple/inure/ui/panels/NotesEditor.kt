package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.viewmodels.panels.NotesEditorViewModel

class NotesEditor : ScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var saving: TypeFaceTextView
    private lateinit var text: TypeFaceEditTextDynamicCorner

    private lateinit var notesViewModel: NotesEditorViewModel
    private var notesPackageInfo: NotesPackageInfo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes_viewer, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        saving = view.findViewById(R.id.saveing_state)
        text = view.findViewById(R.id.app_notes_edit_text)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        val factory = NotesViewModelFactory(requireApplication(), packageInfo)
        notesViewModel = ViewModelProvider(this, factory)[NotesEditorViewModel::class.java]

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(packageInfo.packageName)
        name.text = packageInfo.applicationInfo.name
        packageId.text = PackageUtils.getApplicationVersion(requireContext(), packageInfo)
        saving.setText(R.string.not_available)

        notesViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            text.setText(it.note)
        }

        notesViewModel.getSavedState().observe(viewLifecycleOwner) {
            saving.setText(R.string.saved_successfully)
        }

        text.doOnTextChanged { text, _, _, _ ->
            handler.removeCallbacksAndMessages(null)
            saving.setText(R.string.changes_not_yet_saved)

            if (notesPackageInfo.isNull()) {
                notesPackageInfo = NotesPackageInfo(
                        packageInfo,
                        text.toString(),
                        System.currentTimeMillis(),
                        System.currentTimeMillis())
            } else {
                notesPackageInfo?.dateUpdated = System.currentTimeMillis()

                if (notesPackageInfo?.note != text.toString()) {
                    notesPackageInfo?.note = text.toString()
                } else {
                    saving.setText(R.string.no_changes)
                    return@doOnTextChanged
                }
            }

            handler
                .postDelayed({
                                 saving.setText(R.string.saving)
                                 notesViewModel.updateNoteData(notesPackageInfo!!, 500)
                             }, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): NotesEditor {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = NotesEditor()
            fragment.arguments = args
            return fragment
        }
    }
}