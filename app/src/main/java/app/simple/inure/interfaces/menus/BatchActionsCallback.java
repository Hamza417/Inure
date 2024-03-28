package app.simple.inure.interfaces.menus;

import android.view.View;

import androidx.annotation.NonNull;

public interface BatchActionsCallback {
    void onBatchMenuItemClicked(int id, @NonNull View view);
}
