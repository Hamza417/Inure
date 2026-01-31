package app.simple.inure.ui.editor

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.UnderlineSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ime.InsetsAnimationLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeLinearLayout
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.notes.NotesEditorMenu
import app.simple.inure.dialogs.notes.NotesNotSavedWarning.Companion.showNoteNotSavedWarning
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.factories.panels.NotesViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.popups.notes.PopupBackgroundSpan
import app.simple.inure.popups.notes.PopupTextSize
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.text.EditTextHelper.clearHighlight
import app.simple.inure.text.EditTextHelper.getCurrentTextSize
import app.simple.inure.text.EditTextHelper.highlightText
import app.simple.inure.text.EditTextHelper.resetTextSize
import app.simple.inure.text.EditTextHelper.setTextSize
import app.simple.inure.text.EditTextHelper.toBold
import app.simple.inure.text.EditTextHelper.toItalics
import app.simple.inure.text.EditTextHelper.toStrikethrough
import app.simple.inure.text.EditTextHelper.toSubscript
import app.simple.inure.text.EditTextHelper.toSuperscript
import app.simple.inure.text.EditTextHelper.toUnderline
import app.simple.inure.text.TextViewUndoRedo
import app.simple.inure.util.DateUtils
import app.simple.inure.util.FileUtils.toFileOrNull
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.NotesEditorViewModel
import app.simple.inure.viewmodels.panels.NotesViewModel
import com.anggrayudi.storage.extension.launchOnUiThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesEditor : KeyboardScopedFragment() {

    private lateinit var icon: ImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var noteEditText: TypeFaceEditText
    private lateinit var undo: DynamicRippleImageButton
    private lateinit var redo: DynamicRippleImageButton
    private lateinit var save: DynamicRippleImageButton
    private lateinit var settings: DynamicRippleImageButton
    private lateinit var formattingStrip: ThemeLinearLayout
    private lateinit var container: InsetsAnimationLinearLayout

    private lateinit var bold: DynamicRippleImageButton
    private lateinit var italic: DynamicRippleImageButton
    private lateinit var underline: DynamicRippleImageButton
    private lateinit var strikethrough: DynamicRippleImageButton
    private lateinit var superScript: DynamicRippleImageButton
    private lateinit var subScript: DynamicRippleImageButton
    private lateinit var paint: DynamicRippleImageButton
    private lateinit var size: DynamicRippleImageButton
    private lateinit var date: DynamicRippleImageButton

    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notesEditorViewModel: NotesEditorViewModel
    private var notesPackageInfo: NotesPackageInfo? = null
    private var textViewUndoRedo: TextViewUndoRedo? = null
    private var originalText: SpannableStringBuilder = SpannableStringBuilder("")
    private var customTextWatcher: CustomTextWatcher? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_note_editor, container, false)

        icon = view.findViewById(R.id.app_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        noteEditText = view.findViewById(R.id.app_notes_edit_text)
        undo = view.findViewById(R.id.undo)
        redo = view.findViewById(R.id.redo)
        save = view.findViewById(R.id.save)
        settings = view.findViewById(R.id.settings)
        formattingStrip = view.findViewById(R.id.formatting_strip)
        this.container = view.findViewById(R.id.container)

        bold = view.findViewById(R.id.bold)
        italic = view.findViewById(R.id.italic)
        underline = view.findViewById(R.id.underline)
        strikethrough = view.findViewById(R.id.strike_thru)
        superScript = view.findViewById(R.id.super_script)
        subScript = view.findViewById(R.id.sub_script)
        paint = view.findViewById(R.id.paint)
        size = view.findViewById(R.id.size)
        date = view.findViewById(R.id.date)

        val factory = NotesViewModelFactory(packageInfo)
        notesEditorViewModel = ViewModelProvider(this, factory)[NotesEditorViewModel::class.java]
        notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        customTextWatcher = CustomTextWatcher(this)
        this.icon.transitionName = packageInfo.packageName

        //        sharedElementEnterTransition = MaterialContainerTransform().apply {
        //            duration = resources.getInteger(R.integer.animation_duration).toLong()
        //            setAllContainerColors(Color.TRANSPARENT)
        //            scrimColor = Color.TRANSPARENT
        //            isElevationShadowEnabled = false
        //        }

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fullVersionCheck()

        icon.loadAppIcon(packageInfo.packageName, packageInfo.safeApplicationInfo.enabled, packageInfo.safeApplicationInfo.sourceDir.toFileOrNull())
        name.text = packageInfo.safeApplicationInfo.name
        packageId.text = packageInfo.packageName
        noteEditText.setWindowInsetsAnimationCallback()

        if (StatusBarHeight.isLandscape(requireContext())) {
            val params: FrameLayout.LayoutParams = formattingStrip.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.CENTER_HORIZONTAL
            formattingStrip.layoutParams = params
        }

        noteEditText.addTextChangedListener(customTextWatcher)

        noteEditText.addOnSelectionChangedListener(object : TypeFaceEditText.Companion.OnSelectionChangedListener {
            override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
                val boldSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
                val italicSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
                val underlineSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, UnderlineSpan::class.java)
                val strikethroughSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, StrikethroughSpan::class.java)
                val superScriptSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, SuperscriptSpan::class.java)
                val subScriptSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, SubscriptSpan::class.java)
                val backgroundColorSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, BackgroundColorSpan::class.java)
                val absoluteSizeSpan = noteEditText.editableText!!.getSpans(selectionStart, selectionEnd, AbsoluteSizeSpan::class.java)

                if (boldSpan.isNotEmpty()) {
                    for (span in boldSpan) {
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
                size.setDefaultBackground(absoluteSizeSpan.isNotEmpty())
            }
        })

        notesEditorViewModel.getNoteData().observe(viewLifecycleOwner) {
            notesPackageInfo = it
            originalText = SpannableStringBuilder(it.note)

            val params = TextViewCompat.getTextMetricsParams(noteEditText)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val precomputedText = PrecomputedTextCompat.create(it.note, params)
                launchOnUiThread {
                    noteEditText.setText(precomputedText, TextView.BufferType.SPANNABLE)
                    textViewUndoRedo?.clearHistory()
                    textViewUndoRedo = TextViewUndoRedo(noteEditText)
                }
            }
        }

        bold.setOnClickListener {
            handleFormattingChange {
                noteEditText.toBold()
            }
        }

        italic.setOnClickListener {
            handleFormattingChange {
                noteEditText.toItalics()
            }
        }

        underline.setOnClickListener {
            handleFormattingChange {
                noteEditText.toUnderline()
            }
        }

        strikethrough.setOnClickListener {
            handleFormattingChange {
                noteEditText.toStrikethrough()
            }
        }

        superScript.setOnClickListener {
            handleFormattingChange {
                noteEditText.toSuperscript()
            }
        }

        subScript.setOnClickListener {
            handleFormattingChange {
                noteEditText.toSubscript()
            }
        }

        paint.setOnClickListener {
            PopupBackgroundSpan(it).setOnPopupBackgroundCallbackListener(
                    object : PopupBackgroundSpan.Companion.PopupBackgroundSpanCallback {
                        override fun onColorClicked(color: Int) {
                            handleFormattingChange {
                                noteEditText.highlightText(color)
                            }
                        }

                        override fun onClearClicked() {
                            handleFormattingChange {
                                noteEditText.clearHighlight()
                            }
                        }
                    })
        }

        size.setOnClickListener {
            PopupTextSize(it, noteEditText.getCurrentTextSize()).onSizeChanged = {
                handleFormattingChange {
                    noteEditText.setTextSize(it)
                }
            }
        }

        size.setOnLongClickListener {
            noteEditText.resetTextSize()
            true
        }

        date.setOnClickListener {
            // Insert today's date
            val selectionStart = noteEditText.selectionStart.coerceAtLeast(0)
            val selectionEnd = noteEditText.selectionEnd.coerceAtLeast(0)
            noteEditText.text?.replace(
                    selectionStart.coerceAtMost(selectionEnd),
                    selectionStart.coerceAtLeast(selectionEnd),
                    DateUtils.getTodayDate(),
                    0,
                    DateUtils.getTodayDate().length)
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
                originalText = SpannableStringBuilder(noteEditText.text)
                save.gone(true)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(NOTES_UPDATED))
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
            if (originalText.toString() == noteEditText.text.toString()) {
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
        noteEditText.text?.let {
            doAfterTextChanged(noteEditText.editableText!!, true)
        }
    }

    private fun doAfterTextChanged(editable: Editable, isFormatting: Boolean = false) {
        if (originalText.toString() != editable.toString() || isFormatting) {
            handler.removeCallbacksAndMessages(null)
            save.visible(true)
            postDelayed(1000) {
                handleTextChange(NotesPreferences.isAutoSave())
            }
        } else {
            save.gone(true)
        }
    }

    private fun handleTextChange(save: Boolean) {
        this.save.visible(true)

        if (notesPackageInfo.isNull()) {
            notesPackageInfo = NotesPackageInfo(
                    packageInfo,
                    SpannableStringBuilder(noteEditText.text?.trimEnd()),
                    System.currentTimeMillis(),
                    System.currentTimeMillis())
        } else {
            notesPackageInfo?.note = SpannableStringBuilder(noteEditText.text)
        }

        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false

        if (save) {
            notesEditorViewModel.updateNoteData(notesPackageInfo!!)
            notesViewModel.refreshNotes()
        }
    }

    private fun undoRedoButtonState() {
        undo.isEnabled = textViewUndoRedo?.canUndo ?: false
        redo.isEnabled = textViewUndoRedo?.canRedo ?: false
    }

    private open class CustomTextWatcher(private val notesEditor: NotesEditor) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            /* no-op */
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            notesEditor.doAfterTextChanged(s as Editable)
        }

        override fun afterTextChanged(s: Editable?) {
            /* no-op */
        }

        //        fun callOnTextChanged(editable: Editable, b: Boolean) {
        //            onTextChanged(editable, 0, 0, 0)
        //        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NotesPreferences.AUTO_SAVE -> {
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
            args.putParcelable(BundleConstants.PACKAGE_INFO, packageInfo)
            val fragment = NotesEditor()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "notes_editor"
        const val NOTES_UPDATED = "notes_updated"
    }
}
