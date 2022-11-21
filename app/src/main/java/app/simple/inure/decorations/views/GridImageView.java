package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import app.simple.inure.R;
import app.simple.inure.constants.GridConstants;
import app.simple.inure.decorations.ripple.DynamicRippleImageButton;
import app.simple.inure.preferences.MainPreferences;

public class GridImageView extends DynamicRippleImageButton {
    
    public GridImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setGridIcon(int grid, boolean animate) {
        switch (grid) {
            case GridConstants.grid1:
                setIcon(R.drawable.ic_grid_1, animate);
                break;
            case GridConstants.grid2:
                setIcon(R.drawable.ic_grid_2, animate);
                break;
            case GridConstants.grid3:
                setIcon(R.drawable.ic_grid_3, animate);
                break;
            case GridConstants.grid4:
                setIcon(R.drawable.ic_grid_4, animate);
                break;
        }
    }
    
    public void setGridIcon(int grid, int color) {
        switch (grid) {
            case GridConstants.grid1:
                setImageResource(app.simple.inure.R.drawable.ic_grid_1);
                break;
            case GridConstants.grid2:
                setImageResource(app.simple.inure.R.drawable.ic_grid_2);
                break;
            case GridConstants.grid3:
                setImageResource(app.simple.inure.R.drawable.ic_grid_3);
                break;
            case GridConstants.grid4:
                setImageResource(app.simple.inure.R.drawable.ic_grid_4);
                break;
        }
        
        setColorFilter(color);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case MainPreferences.gridType:
                setGridIcon(sharedPreferences.getInt(key, 1), true);
                break;
        }
    }
}
