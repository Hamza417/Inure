package app.simple.inure.decorations.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;

import app.simple.inure.preferences.AnalyticsPreferences;
import app.simple.inure.preferences.AppearancePreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.TypeFace;

public class ThemePieChart extends PieChart implements SharedPreferences.OnSharedPreferenceChangeListener, ThemeChangedListener {
    
    @SuppressWarnings ("FieldCanBeLocal")
    private final float chartOffset = 20F;
    
    public ThemePieChart(Context context) {
        super(context);
        initProps();
    }
    
    public ThemePieChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProps();
    }
    
    public ThemePieChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initProps();
    }
    
    private void initProps() {
        /*
         * Chart props
         */
        setHoleRadius(AnalyticsPreferences.INSTANCE.getPieHoleRadiusValue());
        setHoleColor(Color.TRANSPARENT);
        setUsePercentValues(false);
        setDragDecelerationFrictionCoef(0.95F);
        setHighlightPerTapEnabled(true);
        getDescription().setEnabled(false);
        setExtraOffsets(chartOffset, chartOffset, chartOffset, chartOffset);
        setDrawCenterText(false);
        
        /*
         * Legend's props
         */
        getLegend().setEnabled(true);
        getLegend().setFormSize(10F);
        getLegend().setFormToTextSpace(5F);
        getLegend().setForm(Legend.LegendForm.DEFAULT);
        getLegend().setTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
        getLegend().setXEntrySpace(20F);
        getLegend().setYEntrySpace(5F);
        getLegend().setTypeface(TypeFace.INSTANCE.getTypeFace(AppearancePreferences.INSTANCE.getAppFont(), TypeFace.TypefaceStyle.MEDIUM.getStyle(), getContext()));
        getLegend().setWordWrapEnabled(true);
        getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
    }
    
    public void startAnimation() {
        animateXY(1000, 500, Easing.EaseOutCubic);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (key) {
            case AnalyticsPreferences.pieHoleRadius: {
                setHoleRadius(AnalyticsPreferences.INSTANCE.getPieHoleRadiusValue());
                invalidate();
            }
        }
    }
    
    @Override
    public void onThemeChanged(Theme theme) {
        ThemeChangedListener.super.onThemeChanged(theme);
        getLegend().setTextColor(theme.getTextViewTheme().getSecondaryTextColor());
        invalidate();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        ThemeManager.INSTANCE.removeListener(this);
    }
}