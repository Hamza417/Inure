package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.simple.inure.R;
import app.simple.inure.constants.GridConstants;
import app.simple.inure.decorations.ripple.DynamicRippleTextView;
import app.simple.inure.preferences.MainPreferences;

public class GridTextView extends DynamicRippleTextView {
    
    public GridTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public GridTextView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public void setGridText(int grid) {
        switch (grid) {
            case GridConstants.grid1:
                setText(R.string.grid_1);
                break;
            case GridConstants.grid2:
                setText(R.string.grid_2);
                break;
            case GridConstants.grid3:
                setText(R.string.grid_3);
                break;
            case GridConstants.grid4:
                setText(R.string.grid_4);
                break;
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case MainPreferences.gridType:
                setGridText(sharedPreferences.getInt(key, 1));
                break;
        }
    }
}
