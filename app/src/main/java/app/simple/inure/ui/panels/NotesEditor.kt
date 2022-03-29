package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditTextDynamicCorner
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.helper.EditTextHelper.toBold
import app.simple.inure.helper.TextViewUndoRedo
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.NotesEditorViewModel

class NotesEditor : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var loader: LoaderImageView
    private lateinit var text: TypeFaceEditTextDynamicCorner

    private lateinit var bold: DynamicRippleImageButton
    private lateinit var undo: DynamicRippleImageButton
    private lateinit var redo: DynamicRippleImageButton

    private lateinit var notesViewModel: NotesEditorViewModel
    private var notesPackageInfo: NotesPackageInfo? = null
    private var textViewUndoRedo: TextViewUndoRedo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes_viewer, container, false)

        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        loader = view.findViewById(R.id.loader)
        text = view.findViewById(R.id.app_notes_edit_text)

        bold = view.findViewById(R.id.bold)
        undo = view.findViewById(R.id.undo)
        redo = view.findViewById(R.id.redo)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!
        val factory = NotesViewModelFactory(requireApplication(), packageInfo)
        notesViewModel = ViewModelProvider(this, factory)[NotesEditorViewModel::class.java]

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = packageInfo.applicationInfo.name
        packageId.text = packageInfo.packageName

        text.doOnTextChanged { text, _, _, _ ->
            handler.removeCallbacksAndMessages(null)
            loader.invisible(true)

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
                    loader.invisible(true)
                    return@doOnTextChanged
                }
            }

            undo.isEnabled = textViewUndoRedo?.canUndo ?: false
            redo.isEnabled = textViewUndoRedo?.canRedo ?: false

            handler
                .postDelayed({
                                 loader.visible(true)
                                 notesViewModel.updateNoteData(notesPackageInfo!!, 500)
                             }, 1000)
        }

        notesViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            text.setText(it.note)
            textViewUndoRedo = TextViewUndoRedo(text)
        }

        notesViewModel.getSavedState().observe(viewLifecycleOwner) {
            loader.invisible(true)
        }

        undo.setOnClickListener {
            if (textViewUndoRedo?.canUndo == true) {
                textViewUndoRedo?.undo()
                undo.isEnabled = textViewUndoRedo?.canUndo ?: false
            }
        }

        redo.setOnClickListener {
            if (textViewUndoRedo?.canRedo == true) {
                textViewUndoRedo?.redo()
                redo.isEnabled = textViewUndoRedo?.canRedo ?: false
            }
        }

        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false

        bold.setOnClickListener {
            if (text.toString().isNotEmpty()) {
                text.toBold()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textViewUndoRedo?.disconnect()
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