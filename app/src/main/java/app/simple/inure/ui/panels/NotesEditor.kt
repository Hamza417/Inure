package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.notes.NotesEditorMenu
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.popups.notes.PopupBackgroundSpan
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.text.EditTextHelper.highlightText
import app.simple.inure.text.EditTextHelper.toBold
import app.simple.inure.text.EditTextHelper.toItalics
import app.simple.inure.text.EditTextHelper.toQuote
import app.simple.inure.text.EditTextHelper.toStrikethrough
import app.simple.inure.text.EditTextHelper.toSubscript
import app.simple.inure.text.EditTextHelper.toSuperscript
import app.simple.inure.text.EditTextHelper.toUnderline
import app.simple.inure.text.SpannableSerializer
import app.simple.inure.text.TextViewUndoRedo
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.NotesEditorViewModel
import app.simple.inure.viewmodels.panels.NotesViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class NotesEditor : KeyboardScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var noteEditText: TypeFaceEditText
    private lateinit var undo: DynamicRippleImageButton
    private lateinit var redo: DynamicRippleImageButton
    private lateinit var save: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notesEditorViewModel: NotesEditorViewModel
    private var notesPackageInfo: NotesPackageInfo? = null
    private var textViewUndoRedo: TextViewUndoRedo? = null

    private val gson: Gson by lazy {
        val type: Type = object : TypeToken<SpannableStringBuilder>() {}.type
        GsonBuilder()
            .registerTypeAdapter(type, SpannableSerializer())
            .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_editor, container, false)

        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        noteEditText = view.findViewById(R.id.app_notes_edit_text)
        undo = view.findViewById(R.id.undo)
        redo = view.findViewById(R.id.redo)
        save = view.findViewById(R.id.save)
        settings = view.findViewById(R.id.settings)

        val factory = NotesViewModelFactory(packageInfo)
        notesEditorViewModel = ViewModelProvider(this, factory)[NotesEditorViewModel::class.java]
        notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = packageInfo.applicationInfo.name
        packageId.text = packageInfo.packageName
        noteEditText.setWindowInsetsAnimationCallback()
        handleFormattingState()

        noteEditText.doAfterTextChanged {
            handler.removeCallbacksAndMessages(null)
            save.visible(true)
            handler.postDelayed(
                    {
                        Log.d("NotesEditor", "Saving notes")
                        handleTextChange(NotesPreferences.isAutoSave())
                    }, 1000)
        }

        notesEditorViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            if (NotesPreferences.areJSONSpans()) {
                noteEditText.setText(gson.toJson(it.note), TextView.BufferType.SPANNABLE)
            } else {
                noteEditText.setText(it.note, TextView.BufferType.SPANNABLE)
            }
            textViewUndoRedo?.clearHistory()
            textViewUndoRedo = TextViewUndoRedo(noteEditText)
        }

        /**
         * It could be null, I mean why not :P
         */
        bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getNotesFunctionMenu(),
                                                              null /* We don't do that here*/) { id, view ->
            val start = noteEditText.selectionStart
            val beforeChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable
            when (id) {
                R.drawable.ic_format_bold -> {
                    noteEditText.toBold()
                }
                R.drawable.ic_format_italic -> {
                    noteEditText.toItalics()
                }
                R.drawable.ic_format_underlined -> {
                    noteEditText.toUnderline()
                }
                R.drawable.ic_format_strikethrough -> {
                    noteEditText.toStrikethrough()
                }
                R.drawable.ic_format_superscript -> {
                    noteEditText.toSuperscript()
                }
                R.drawable.ic_format_subscript -> {
                    noteEditText.toSubscript()
                }
                R.drawable.ic_format_quote -> {
                    noteEditText.toQuote()
                }
                R.drawable.ic_format_paint -> {
                    PopupBackgroundSpan(view).setOnPopupBackgroundCallbackListener(
                            object : PopupBackgroundSpan.Companion.PopupBackgroundSpanCallback {
                                override fun onColorClicked(color: Int) {
                                    noteEditText.highlightText(color)
                                }
                            })
                }
            }

            val afterChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable

            textViewUndoRedo?.addHistory(start, beforeChange, afterChange)
            undoRedoButtonState()
        }

        notesEditorViewModel.getSavedState().observe(viewLifecycleOwner) {
            if (it >= 0) {
                save.gone(true)
            }
        }

        undo.setOnClickListener {
            if (textViewUndoRedo?.canUndo == true) {
                textViewUndoRedo?.undo()
                undoRedoButtonState()
            }
        }

        redo.setOnClickListener {
            if (textViewUndoRedo?.canRedo == true) {
                textViewUndoRedo?.redo()
                undoRedoButtonState()
            }
        }

        save.setOnClickListener {
            handleTextChange(save = true)
        }

        settings.setOnClickListener {
            NotesEditorMenu.newInstance()
                .show(childFragmentManager, "notes_editor_menu")
        }

        notesEditorViewModel.getError().observe(viewLifecycleOwner) {
            showError(it)
        }

        undoRedoButtonState()
    }

    private fun handleTextChange(save: Boolean) {
        this.save.visible(true)

        if (notesPackageInfo.isNull()) {
            notesPackageInfo = NotesPackageInfo(
                    packageInfo,
                    SpannableStringBuilder(noteEditText.text),
                    System.currentTimeMillis(),
                    System.currentTimeMillis())
        } else {
            if (NotesPreferences.areJSONSpans()) {
                kotlin.runCatching {
                    notesPackageInfo?.note = gson.fromJson(noteEditText.text.toString(), SpannableStringBuilder::class.java)
                }.onFailure {
                    notesPackageInfo?.note = SpannableStringBuilder(noteEditText.text)
                }
            } else {
                notesPackageInfo?.note = SpannableStringBuilder(noteEditText.text)
            }
        }

        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false

        if (save) {
            Log.d(javaClass.simpleName, "Saving note")
            notesEditorViewModel.updateNoteData(notesPackageInfo!!)
            notesViewModel.refreshNotes()
        }
    }

    private fun handleFormattingState() {
        if (NotesPreferences.areJSONSpans()) {
            bottomRightCornerMenu?.gone()
        } else {
            bottomRightCornerMenu?.visible(false)
        }
    }

    private fun undoRedoButtonState() {
        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NotesPreferences.jsonSpans -> {
                notesEditorViewModel.refresh()
                handleFormattingState()
            }
            NotesPreferences.autoSave -> {
                if (!NotesPreferences.isAutoSave()) {
                    save.visible(true)
                } else {
                    handleTextChange(save = true)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textViewUndoRedo?.disconnect()
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