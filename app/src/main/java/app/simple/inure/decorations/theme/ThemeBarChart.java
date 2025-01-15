package app.simple.inure.decorations.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;

import androidx.annotation.NonNull;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Accent;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.StatusBarHeight;
import app.simple.inure.util.TypeFace;

public class ThemeBarChart extends BarChart implements SharedPreferences.OnSharedPreferenceChangeListener, ThemeChangedListener {
    
    @SuppressWarnings ("FieldCanBeLocal")
    private final float chartOffset = 20F;
    
    public ThemeBarChart(Context context) {
        super(context);
        initProps();
    }
    
    public ThemeBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProps();
    }
    
    public ThemeBarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initProps();
    }
    
    private void initProps() {
        setDrawBarShadow(false);
        setDrawValueAboveBar(true);
        setDrawBorders(false);
        setDrawMarkers(true);
        setDrawGridBackground(false);
        setPinchZoom(false);
        setDoubleTapToZoomEnabled(false);
        getDescription().setEnabled(false);
        
        getAxisLeft().setEnabled(false);
        getAxisRight().setEnabled(false);
        getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        getXAxis().setDrawGridLines(false);
        getXAxis().setDrawLabels(true);
        getXAxis().setGranularity(1f);
        
        if (!isInEditMode()) {
            setNoDataTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
            setNoDataTextTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
            
            getXAxis().setTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
            getXAxis().setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
            
            getLegend().setTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
            getLegend().setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
        }
        
        if (StatusBarHeight.isLandscape(getContext())) {
            setExtraOffsets(chartOffset * 2F, chartOffset * 2F, chartOffset * 2F, chartOffset * 2F);
            setExtraRightOffset(chartOffset * 4F);
        } else {
            setExtraOffsets(chartOffset, chartOffset, chartOffset, chartOffset);
        }
        
        getLegend().setForm(Legend.LegendForm.CIRCLE);
        getLegend().setFormSize(10f);
        getLegend().setFormToTextSpace(5f);
        getLegend().setXEntrySpace(20F);
        getLegend().setYEntrySpace(5F);
        getLegend().setWordWrapEnabled(true);
        
        if (StatusBarHeight.isLandscape(getContext())) {
            getLegend().setXOffset(chartOffset * 3F);
            getLegend().setOrientation(Legend.LegendOrientation.VERTICAL);
            getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
            getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        } else {
            getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
            getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        ThemeManager.INSTANCE.removeListener(this);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        initProps();
        invalidate();
    }
    
    @Override
    public void onAccentChanged(@NonNull Accent accent) {
        initProps();
        invalidate();
    }
}
