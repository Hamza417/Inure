package app.simple.inure.interfaces.adapters;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import app.simple.inure.models.PreferenceModel;

public interface PreferencesCallbacks {
    default void onPrefsClicked(@NonNull ImageView imageView, int category, int position) {
    
    }
    
    default void onPrefsSearchItemClicked(PreferenceModel preferenceModel) {
    
    }
}
