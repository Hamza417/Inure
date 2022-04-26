package app.simple.inure.interfaces.adapters;

import android.content.pm.PackageInfo;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import app.simple.inure.models.BatchPackageInfo;
import app.simple.inure.models.NotesPackageInfo;

public interface AppsAdapterCallbacks {
    default void onAppClicked(@NonNull PackageInfo packageInfo, @NonNull ImageView icon) {
    
    }
    
    default void onAppLongPressed(@NonNull PackageInfo packageInfo, @NonNull ImageView icon) {
    
    }
    
    default void onSearchPressed(@NonNull View view) {
    
    }
    
    default void onFilterPressed(@NonNull View view) {
    
    }
    
    default void onSortPressed(@NonNull View view) {
    
    }
    
    default void onSettingsPressed(@NonNull View view) {
    
    }
    
    default void onInfoPressed(@NonNull View view) {
    
    }
    
    default void onNoteDelete(@NonNull View view, NotesPackageInfo notesPackageInfo) {
    
    }
    
    default void onBatchChanged(@NonNull BatchPackageInfo batchPackageInfo) {
    
    }
    
    default void onNoteClicked(@NonNull NotesPackageInfo notesPackageInfo) {
    
    }
    
    default void onNoteLongClicked(@NonNull NotesPackageInfo notesPackageInfo) {
    
    }
}
