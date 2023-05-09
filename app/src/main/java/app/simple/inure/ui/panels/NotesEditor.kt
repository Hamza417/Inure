package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.style.*
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.notes.NotesEditorMenu
import app.simple.inure.dialogs.notes.NotesNotSavedWarning.Companion.showNoteNotSavedWarning
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.popups.notes.PopupBackgroundSpan
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.text.EditTextHelper.highlightText
import app.simple.inure.text.EditTextHelper.toBold
import app.simple.inure.text.EditTextHelper.toItalics
import app.simple.inure.text.EditTextHelper.toStrikethrough
import app.simple.inure.text.EditTextHelper.toSubscript
import app.simple.inure.text.EditTextHelper.toSuperscript
import app.simple.inure.text.EditTextHelper.toUnderline
import app.simple.inure.text.SpannableSerializer
import app.simple.inure.text.TextViewUndoRedo
import app.simple.inure.util.DateUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.StatusBarHeight
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
    private lateinit var formattingStrip: ThemeLinearLayout

    private lateinit var bold: DynamicRippleImageButton
    private lateinit var italic: DynamicRippleImageButton
    private lateinit var underline: DynamicRippleImageButton
    private lateinit var strikethrough: DynamicRippleImageButton
    private lateinit var superScript: DynamicRippleImageButton
    private lateinit var subScript: DynamicRippleImageButton
    private lateinit var paint: DynamicRippleImageButton
    private lateinit var date: DynamicRippleImageButton

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

    private var isSaved = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_editor, container, false)

        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        noteEditText = view.findViewById(R.id.app_notes_edit_text)
        undo = view.findViewById(R.id.undo)
        redo = view.findViewById(R.id.redo)
        save = view.findViewById(R.id.save)
        settings = view.findViewById(R.id.settings)
        formattingStrip = view.findViewById(R.id.formatting_strip)

        bold = view.findViewById(R.id.bold)
        italic = view.findViewById(R.id.italic)
        underline = view.findViewById(R.id.underline)
        strikethrough = view.findViewById(R.id.strike_thru)
        superScript = view.findViewById(R.id.super_script)
        subScript = view.findViewById(R.id.sub_script)
        paint = view.findViewById(R.id.paint)
        date = view.findViewById(R.id.date)

        val factory = NotesViewModelFactory(packageInfo)
        notesEditorViewModel = ViewModelProvider(this, factory)[NotesEditorViewModel::class.java]
        notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

        name.text = packageInfo.applicationInfo.name
        packageId.text = packageInfo.packageName
        noteEditText.setWindowInsetsAnimationCallback()
        handleFormattingState()

        if (StatusBarHeight.isLandscape(requireContext())) {
            val params: FrameLayout.LayoutParams = formattingStrip.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.CENTER_HORIZONTAL
            formattingStrip.layoutParams = params
        }

        noteEditText.doAfterTextChanged {
            handler.removeCallbacksAndMessages(null)
            save.visible(true)
            isSaved = false
            handler.postDelayed(
                    {
                        Log.d("NotesEditor", "Saving notes")
                        handleTextChange(NotesPreferences.isAutoSave())
                    }, 1000)
        }

        noteEditText.addOnSelectionChangedListener(object : TypeFaceEditText.Companion.OnSelectionChangedListener {
            override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
                Log.d("NotesEditor", "Selection changed, start: $selectionStart, end: $selectionEnd")
                val boldSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
                val italicSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
                val underlineSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, UnderlineSpan::class.java)
                val strikethroughSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StrikethroughSpan::class.java)
                val superScriptSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, SuperscriptSpan::class.java)
                val subScriptSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, SubscriptSpan::class.java)
                val backgroundColorSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, BackgroundColorSpan::class.java)

                if (boldSpan.isNotEmpty()) {
                    for (span in boldSpan) {
                        Log.d("NotesEditor", "Bold span: ${span.style}")
                        if (span.style == Typeface.BOLD) {
                            bold.setDefaultBackground(true)
                            break
                        } else {
                            bold.setDefaultBackground(false)
                        }
                    }
                } else {
                    bold.setDefaultBackground(false)
                }

                if (italicSpan.isNotEmpty()) {
                    for (span in italicSpan) {
                        Log.d("NotesEditor", "Italic span: ${span.style}")
                        if (span.style == Typeface.ITALIC) {
                            italic.setDefaultBackground(true)
                            break
                        } else {
                            italic.setDefaultBackground(false)
                        }
                    }
                } else {
                    italic.setDefaultBackground(false)
                }

                underline.setDefaultBackground(underlineSpan.isNotEmpty())
                strikethrough.setDefaultBackground(strikethroughSpan.isNotEmpty())
                superScript.setDefaultBackground(superScriptSpan.isNotEmpty())
                subScript.setDefaultBackground(subScriptSpan.isNotEmpty())
                paint.setDefaultBackground(backgroundColorSpan.isNotEmpty())
            }
        })

        notesEditorViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            if (NotesPreferences.areJSONSpans()) {
                noteEditText.setText(gson.toJson(it.note), TextView.BufferType.SPANNABLE)
            } else {
                noteEditText.setText(it.note, TextView.BufferType.SPANNABLE)
            }
            textViewUndoRedo?.clearHistory()
            isSaved = true // We are setting it to true because we are loading the data from the database
            textViewUndoRedo = TextViewUndoRedo(noteEditText)
        }

        bold.setOnClickListener {
            handleFormattingChange { noteEditText.toBold() }
        }

        italic.setOnClickListener {
            handleFormattingChange { noteEditText.toItalics() }
        }

        underline.setOnClickListener {
            handleFormattingChange { noteEditText.toUnderline() }
        }

        strikethrough.setOnClickListener {
            handleFormattingChange { noteEditText.toStrikethrough() }
        }

        superScript.setOnClickListener {
            handleFormattingChange { noteEditText.toSuperscript() }
        }

        subScript.setOnClickListener {
            handleFormattingChange { noteEditText.toSubscript() }
        }

        paint.setOnClickListener {
            PopupBackgroundSpan(it).setOnPopupBackgroundCallbackListener(
                    object : PopupBackgroundSpan.Companion.PopupBackgroundSpanCallback {
                        override fun onColorClicked(color: Int) {
                            handleFormattingChange { noteEditText.highlightText(color) }
                        }
                    })
        }

        date.setOnClickListener {
            // Insert today's date
            val selectionStart = noteEditText.selectionStart.coerceAtLeast(0)
            val selectionEnd = noteEditText.selectionEnd.coerceAtLeast(0)
            noteEditText.text?.replace(selectionStart.coerceAtMost(selectionEnd), selectionStart.coerceAtLeast(selectionEnd),
                                       DateUtils.getTodayDate(), 0, DateUtils.getTodayDate().length)
        }

        // TODO - There was a unique bug where the bottom menu was not showing up when the user was in the notes editor fragment

        //        /**
        //         * It could be null, I mean why not :P
        //         */
        //        bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getNotesFunctionMenu(),
        //                                                              null /* We don't do that here*/) { id, view1 ->
        //
        //        }

        notesEditorViewModel.getSavedState().observe(viewLifecycleOwner) {
            if (it >= 0) {
                save.gone(true)
                isSaved = true
                Log.d("NotesEditor", "Saved notes")
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isSaved) {
                remove()
                goBack()
            } else {
                childFragmentManager.showNoteNotSavedWarning().setSureCallbacks(object : SureCallbacks {
                    override fun onSure() {
                        popBackStack()
                    }
                })
            }
        }
    }

    private fun handleFormattingChange(function: () -> Unit) {
        val start = noteEditText.selectionStart
        val beforeChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable
        function()
        noteEditText.triggerSelectionChanged()
        val afterChange: Editable = noteEditText.editableText!!.subSequence(start, noteEditText.selectionEnd) as Editable
        textViewUndoRedo?.addHistory(start, beforeChange, afterChange)
        undoRedoButtonState()
    }

    private fun handleTextChange(save: Boolean) {
        this.save.visible(true)
        isSaved = false

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