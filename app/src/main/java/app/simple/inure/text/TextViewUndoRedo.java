package app.simple.inure.text;

/*
 * THIS CLASS IS PROVIDED TO THE PUBLIC DOMAIN FOR FREE WITHOUT ANY
 * RESTRICTIONS OR ANY WARRANTY.
 */

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * A generic undo/redo implementation for TextViews.
 */
public class TextViewUndoRedo {
    
    /**
     * Is undo/redo being performed? This member signals if an undo/redo
     * operation is currently being performed. Changes in the text during
     * undo/redo are not recorded because it would mess up the undo history.
     */
    private boolean isUndoOrRedo = false;
    
    /**
     * The edit history.
     */
    private final EditHistory editHistory;
    
    /**
     * The change listener.
     */
    private final EditTextChangeListener editTextChangeListener;
    
    /**
     * The edit text.
     */
    private final TextView textView;
    
    // =================================================================== //
    
    /**
     * Create a new TextViewUndoRedo and attach it to the specified TextView.
     *
     * @param textView The text view for which the undo/redo is implemented.
     */
    public TextViewUndoRedo(TextView textView) {
        this.textView = textView;
        editHistory = new EditHistory();
        editTextChangeListener = new EditTextChangeListener();
        this.textView.addTextChangedListener(editTextChangeListener);
    }
    
    // =================================================================== //
    
    /**
     * Disconnect this undo/redo from the text view.
     */
    public void disconnect() {
        textView.removeTextChangedListener(editTextChangeListener);
    }
    
    /**
     * Set the maximum history size. If size is negative, then history size is
     * only limited by the device memory.
     */
    public void setMaxHistorySize(int maxHistorySize) {
        editHistory.setMaxHistorySize(maxHistorySize);
    }
    
    /**
     * Clear history.
     */
    public void clearHistory() {
        editHistory.clear();
    }
    
    /**
     * Can undo be performed?
     */
    public boolean getCanUndo() {
        return (editHistory.position > 0);
    }
    
    /**
     * Perform undo.
     */
    public void undo() {
        EditItem edit = editHistory.getPrevious();
        if (edit == null) {
            return;
        }
    
        Editable text = textView.getEditableText();
        int start = edit.startPosition;
        int end = start + (edit.editableAfter != null ? edit.editableAfter.length() : 0);
    
        isUndoOrRedo = true;
        text.replace(start, end, edit.editableBefore);
        isUndoOrRedo = false;
    
        /*
         * This will get rid of underlines inserted when editor tries to come
         * up with a suggestion.
         */
        //        for (UnderlineSpan underlineSpan : text.getSpans(0, text.length(), UnderlineSpan.class)) {
        //            text.removeSpan(underlineSpan);
        //        }
    
        Selection.setSelection(text, edit.editableBefore == null ? start : (start + edit.editableBefore.length()));
    }
    
    /**
     * Can redo be performed?
     */
    public boolean getCanRedo() {
        return (editHistory.position < editHistory.history.size());
    }
    
    /**
     * Perform redo.
     */
    public void redo() {
        EditItem edit = editHistory.getNext();
        if (edit == null) {
            return;
        }
    
        Editable text = textView.getEditableText();
        int start = edit.startPosition;
        int end = start + (edit.editableBefore != null ? edit.editableBefore.length() : 0);
    
        isUndoOrRedo = true;
        text.replace(start, end, edit.editableAfter);
        isUndoOrRedo = false;
    
        /*
         * This will get rid of underlines inserted when editor tries to come
         * up with a suggestion.
         */
        //        for (UnderlineSpan span : text.getSpans(0, text.length(), UnderlineSpan.class)) {
        //            text.removeSpan(span);
        //        }
    
        Selection.setSelection(text, edit.editableAfter == null ? start : (start + edit.editableAfter.length()));
    }
    
    /**
     * Store preferences.
     */
    public void storePersistentState(Editor editor, String prefix) {
        // Store hash code of text in the editor so that we can check if the
        // editor contents has changed.
        editor.putString(prefix + ".hash",
                String.valueOf(textView.getText().toString().hashCode()));
        editor.putInt(prefix + ".maxSize", editHistory.maxHistorySize);
        editor.putInt(prefix + ".position", editHistory.position);
        editor.putInt(prefix + ".size", editHistory.history.size());
    
        int i = 0;
        for (EditItem ei : editHistory.history) {
            String pre = prefix + "." + i;
        
            editor.putInt(pre + ".start", ei.startPosition);
            editor.putString(pre + ".before", ei.editableBefore.toString());
            editor.putString(pre + ".after", ei.editableAfter.toString());
        
            i++;
        }
    }
    
    /**
     * Restore preferences.
     *
     * @param prefix The preference key prefix used when state was stored.
     * @return did restore succeed? If this is false, the undo history will be
     * empty.
     */
    public boolean restorePersistentState(SharedPreferences sp, String prefix)
            throws IllegalStateException {
    
        boolean ok = doRestorePersistentState(sp, prefix);
        if (!ok) {
            editHistory.clear();
        }
    
        return ok;
    }
    
    private boolean doRestorePersistentState(SharedPreferences sharedPreferences, String prefix) {
        String hash = sharedPreferences.getString(prefix + ".hash", null);
        if (hash == null) {
            // No state to be restored.
            return true;
        }
        
        if (Integer.parseInt(hash) != textView.getText().toString().hashCode()) {
            return false;
        }
    
        editHistory.clear();
        editHistory.maxHistorySize = sharedPreferences.getInt(prefix + ".maxSize", -1);
        
        int count = sharedPreferences.getInt(prefix + ".size", -1);
        if (count == -1) {
            return false;
        }
        
        for (int i = 0; i < count; i++) {
            String pre = prefix + "." + i;
            
            int start = sharedPreferences.getInt(pre + ".start", -1);
            SpannableStringBuilder before = new SpannableStringBuilder(sharedPreferences.getString(pre + ".before", null));
            SpannableStringBuilder after = new SpannableStringBuilder(sharedPreferences.getString(pre + ".after", null));
    
            if (start == -1 || before == null || after == null) {
                return false;
            }
    
            editHistory.add(new EditItem(start, before, after));
        }
    
        editHistory.position = sharedPreferences.getInt(prefix + ".position", -1);
        return editHistory.position != -1;
    }
    
    public void addHistory(int start, Editable beforeChange, Editable afterChange) {
        editHistory.add(new EditItem(start, beforeChange, afterChange));
    }
    
    // =================================================================== //
    
    /**
     * Keeps track of all the edit history of a text.
     */
    private static final class EditHistory {
        
        /**
         * The list of edits in chronological order.
         */
        private final LinkedList <EditItem> history = new LinkedList <>();
        /**
         * The position from which an EditItem will be retrieved when getNext()
         * is called. If getPrevious() has not been called, this has the same
         * value as mmHistory.size().
         */
        private int position = 0;
        /**
         * Maximum undo history size.
         */
        private int maxHistorySize = -1;
        
        /**
         * Clear history.
         */
        private void clear() {
            position = 0;
            history.clear();
        }
        
        /**
         * Adds a new edit operation to the history at the current position. If
         * executed after a call to getPrevious() removes all the future history
         * (elements with positions >= current history position).
         */
        private void add(EditItem item) {
            while (history.size() > position) {
                history.removeLast();
            }
            history.add(item);
            position++;
    
            if (maxHistorySize >= 0) {
                trimHistory();
            }
        }
        
        /**
         * Set the maximum history size. If size is negative, then history size
         * is only limited by the device memory.
         */
        private void setMaxHistorySize(int maxHistorySize) {
            this.maxHistorySize = maxHistorySize;
            if (this.maxHistorySize >= 0) {
                trimHistory();
            }
        }
        
        /**
         * Trim history when it exceeds max history size.
         */
        private void trimHistory() {
            while (history.size() > maxHistorySize) {
                history.removeFirst();
                position--;
            }
    
            if (position < 0) {
                position = 0;
            }
        }
        
        /**
         * Traverses the history backward by one position, returns and item at
         * that position.
         */
        private EditItem getPrevious() {
            if (position == 0) {
                return null;
            }
            position--;
            return history.get(position);
        }
        
        /**
         * Traverses the history forward by one position, returns and item at
         * that position.
         */
        private EditItem getNext() {
            if (position >= history.size()) {
                return null;
            }
    
            EditItem item = history.get(position);
            position++;
            return item;
        }
    }
    
    /**
     * Represents the changes performed by a single edit operation.
     */
    private static final class EditItem {
        private final int startPosition;
        private final Editable editableBefore;
        private final Editable editableAfter;
        
        /**
         * Constructs EditItem of a modification that was applied at position
         * start and replaced CharSequence before with CharSequence after.
         */
        public EditItem(int startPosition, Editable before, Editable after) {
            this.startPosition = startPosition;
            this.editableBefore = before;
            editableAfter = after;
        }
    }
    
    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener implements TextWatcher {
    
        /**
         * The text that will be removed by the change event.
         */
        private Editable beforeChange;
    
        /**
         * The text that was inserted by the change event.
         */
        private Editable afterChange;
    
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (isUndoOrRedo) {
                return;
            }
        
            beforeChange = (Editable) s.subSequence(start, start + count);
        }
    
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (isUndoOrRedo) {
                return;
            }
        
            afterChange = (Editable) charSequence.subSequence(start, start + count);
            editHistory.add(new EditItem(start, beforeChange, afterChange));
        }
    
        public void afterTextChanged(Editable s) {
            /*
             * No op
             */
        }
    }
}