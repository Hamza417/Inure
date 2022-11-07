package app.simple.inure.decorations.searchview;

import android.view.View;

import org.jetbrains.annotations.NotNull;

public interface SearchViewEventListener {
    /**
     * Pressed events for search view menu button,
     * it only calls the listener and the listening
     * object will have to implement the further actions
     *
     * @param button button reference to use the view as
     *               anchor or any use that requires
     *               view reference
     */
    void onSearchMenuPressed(@NotNull View button);
    
    /**
     * Search keywords entered in the text box
     *
     * @param keywords are the text entered in the text
     *                 box, keywords are updated immediately
     *                 as they are entered and should be used
     *                 to filter out data from the list and
     *                 update it manually
     * @param count    is the total characters count of the keywords
     */
    void onSearchTextChanged(@NotNull String keywords, int count);
}
