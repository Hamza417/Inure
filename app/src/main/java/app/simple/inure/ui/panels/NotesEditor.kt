package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.toHtml
import androidx.core.text.toSpanned
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterFormattingStrip
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.dialogs.notes.NotesEditorMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.helper.EditTextHelper.addBullet
import app.simple.inure.helper.EditTextHelper.decreaseTextSize
import app.simple.inure.helper.EditTextHelper.increaseTextSize
import app.simple.inure.helper.EditTextHelper.toBold
import app.simple.inure.helper.EditTextHelper.toItalics
import app.simple.inure.helper.EditTextHelper.toStrikethrough
import app.simple.inure.helper.EditTextHelper.toSubscript
import app.simple.inure.helper.EditTextHelper.toSuperscript
import app.simple.inure.helper.EditTextHelper.toUnderline
import app.simple.inure.helper.TextViewUndoRedo
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.TextViewUtils.toHtmlSpanned
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.NotesEditorViewModel
import org.jsoup.Jsoup

class NotesEditor : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var noteEditText: TypeFaceEditText
    private lateinit var undo: DynamicRippleImageButton
    private lateinit var redo: DynamicRippleImageButton
    private lateinit var save: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var formattingStrip: CustomHorizontalRecyclerView

    private lateinit var notesViewModel: NotesEditorViewModel
    private var notesPackageInfo: NotesPackageInfo? = null
    private var textViewUndoRedo: TextViewUndoRedo? = null

    private val bold = 1
    private val italics = 2
    private val underline = 3
    private val strikethrough = 4
    private val decrease = 5
    private val increase = 6
    private val bullet = 7
    private val superscript = 8
    private val subscripts = 9

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes_viewer, container, false)

        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        noteEditText = view.findViewById(R.id.app_notes_edit_text)
        undo = view.findViewById(R.id.undo)
        redo = view.findViewById(R.id.redo)
        save = view.findViewById(R.id.save)
        settings = view.findViewById(R.id.settings)
        formattingStrip = view.findViewById(R.id.formatting_strip_rv)

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
        noteSpanTypeUpdate()

        noteEditText.doOnTextChanged { text, _, _, _ ->
            handleTextChange()
        }

        notesViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            if (NotesPreferences.areHTMLSpans()) {
                noteEditText.setText(it.note.toHtmlSpanned().toHtml())
            } else {
                noteEditText.setText(it.note.toHtmlSpanned())
            }
            textViewUndoRedo = TextViewUndoRedo(noteEditText)
        }

        notesViewModel.getFormattingStrip().observe(viewLifecycleOwner) { list ->
            val adapterFormattingStrip = AdapterFormattingStrip(list)

            adapterFormattingStrip.setOnFormattingStripCallbackListener(object : AdapterFormattingStrip.Companion.FormattingStripCallbacks {
                override fun onFormattingButtonClicked(position: Int) {
                    kotlin.runCatching {
                        when (position) {
                            bold -> {
                                noteEditText.toBold()
                            }
                            italics -> {
                                noteEditText.toItalics()
                            }
                            underline -> {
                                noteEditText.toUnderline()
                            }
                            strikethrough -> {
                                noteEditText.toStrikethrough()
                            }
                            decrease -> {
                                noteEditText.decreaseTextSize()
                            }
                            increase -> {
                                noteEditText.increaseTextSize()
                            }
                            bullet -> {
                                noteEditText.addBullet()
                            }
                            superscript -> {
                                noteEditText.toSuperscript()
                            }
                            subscripts -> {
                                noteEditText.toSubscript()
                            }
                        }
                    }.getOrElse {
                        printError(it)
                    }
                }
            })

            formattingStrip.adapter = adapterFormattingStrip
        }

        notesViewModel.getSavedState().observe(viewLifecycleOwner) {
            if (it >= 0) {
                save.gone(true)
            }
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

        save.setOnClickListener {
            handleTextChange()
        }

        settings.setOnClickListener {
            NotesEditorMenu.newInstance()
                .show(childFragmentManager, "notes_editor_menu")
        }
    }

    private fun handleTextChange() {
        handler.removeCallbacksAndMessages(null)
        save.visible(true)

        println(noteEditText.text?.toString())

        if (notesPackageInfo.isNull()) {
            notesPackageInfo = NotesPackageInfo(
                    packageInfo,
                    if (NotesPreferences.areHTMLSpans()) {
                        Jsoup.parse(noteEditText.text?.toString()!!).toString()
                    } else {
                        noteEditText.text?.toSpanned()?.toHtml()
                    },
                    System.currentTimeMillis(),
                    System.currentTimeMillis())
        } else {
            if (NotesPreferences.areHTMLSpans()) {
                notesPackageInfo?.note = Jsoup.parse(noteEditText.text?.toHtml()!!).toString()
            } else {
                notesPackageInfo?.note = noteEditText.text?.toSpanned()!!.toHtml()
            }
        }

        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false

        handler
            .postDelayed({
                             notesViewModel.updateNoteData(notesPackageInfo!!, 500)
                         }, 1000)
    }

    private fun printError(throwable: Throwable) {
        val e = Error.newInstance(throwable.stackTraceToString())
        e.show(childFragmentManager, "error_dialog")
        e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
            override fun onDismiss() {
                requireActivity().onBackPressed()
            }
        })
    }

    private fun noteSpanTypeUpdate() {
        if (NotesPreferences.areHTMLSpans()) {
            formattingStrip.gone()
            noteEditText.setText(notesPackageInfo?.note)
        } else {
            formattingStrip.visible(true)
            noteEditText.setText(notesPackageInfo?.note?.toHtmlSpanned())
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NotesPreferences.htmlSpans -> {
                noteSpanTypeUpdate()
                notesViewModel.refresh()
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