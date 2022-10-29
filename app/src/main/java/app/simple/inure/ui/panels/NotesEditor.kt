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
import app.simple.inure.adapters.details.AdapterFormattingStrip
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomHorizontalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.notes.NotesEditorMenu
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.popups.notes.PopupBackgroundSpan
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.text.EditTextHelper.addBullet
import app.simple.inure.text.EditTextHelper.blur
import app.simple.inure.text.EditTextHelper.decreaseTextSize
import app.simple.inure.text.EditTextHelper.highlightText
import app.simple.inure.text.EditTextHelper.increaseTextSize
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
    private lateinit var formattingStrip: CustomHorizontalRecyclerView

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notesEditorViewModel: NotesEditorViewModel
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
    private val backgroundSpan = 10
    private val quote = 11
    private val blur = 12

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
        formattingStrip = view.findViewById(R.id.formatting_strip_rv)

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

        notesEditorViewModel.getFormattingStrip().observe(viewLifecycleOwner) { list ->
            val adapterFormattingStrip = AdapterFormattingStrip(list)
            adapterFormattingStrip.setOnFormattingStripCallbackListener(object : AdapterFormattingStrip.Companion.FormattingStripCallbacks {
                override fun onFormattingButtonClicked(position: Int, view: View) {
                    kotlin.runCatching {
                        val start = noteEditText.selectionStart
                        val beforeChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable

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
                            quote -> {
                                noteEditText.toQuote()
                            }
                            backgroundSpan -> {
                                PopupBackgroundSpan(view).setOnPopupBackgroundCallbackListener(
                                        object : PopupBackgroundSpan.Companion.PopupBackgroundSpanCallback {
                                            override fun onColorClicked(color: Int) {
                                                noteEditText.highlightText(color)
                                            }
                                        })
                            }
                            blur -> {
                                noteEditText.blur()
                            }
                        }

                        val afterChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable

                        textViewUndoRedo?.addHistory(start, beforeChange, afterChange)
                        undoRedoButtonState()
                    }.getOrElse {
                        showError(it)
                    }
                }
            })

            formattingStrip.adapter = adapterFormattingStrip
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
            formattingStrip.gone()
        } else {
            formattingStrip.visible(false)
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