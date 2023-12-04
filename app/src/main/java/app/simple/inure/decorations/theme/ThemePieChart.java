package app.simple.inure.decorations.theme;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;

import androidx.annotation.NonNull;
import app.simple.inure.R;
import app.simple.inure.preferences.AccessibilityPreferences;
import app.simple.inure.preferences.AnalyticsPreferences;
import app.simple.inure.themes.interfaces.ThemeChangedListener;
import app.simple.inure.themes.manager.Theme;
import app.simple.inure.themes.manager.ThemeManager;
import app.simple.inure.util.NullSafety;
import app.simple.inure.util.StatusBarHeight;
import app.simple.inure.util.TypeFace;

public class ThemePieChart extends PieChart implements SharedPreferences.OnSharedPreferenceChangeListener, ThemeChangedListener {
    
    @SuppressWarnings ("FieldCanBeLocal")
    private final float chartOffset = 20F;
    private ValueAnimator valueAnimator;
    private boolean animate = true;
    
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
        if (!isInEditMode()) {
            setHoleRadius(AnalyticsPreferences.INSTANCE.getPieHoleRadiusValue());
            setNoDataTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
            setNoDataTextTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
        }
    
        setHoleColor(Color.TRANSPARENT);
        setUsePercentValues(false);
        setDragDecelerationFrictionCoef(0.95F);
        setHighlightPerTapEnabled(true);
        getDescription().setEnabled(false);
        setDrawCenterText(false);
    
        if (StatusBarHeight.isLandscape(getContext())) {
            setExtraOffsets(chartOffset * 2F, chartOffset * 2F, chartOffset * 2F, chartOffset * 2F);
            setExtraRightOffset(chartOffset * 4F);
        } else {
            setExtraOffsets(chartOffset, chartOffset, chartOffset, chartOffset);
        }
    
        /*
         * Legend's props
         */
        getLegend().setEnabled(false);
        getLegend().setFormSize(10F);
        getLegend().setFormToTextSpace(5F);
        getLegend().setForm(Legend.LegendForm.DEFAULT);
        getLegend().setTextColor(ThemeManager.INSTANCE.getTheme().getTextViewTheme().getSecondaryTextColor());
        getLegend().setXEntrySpace(20F);
        getLegend().setYEntrySpace(5F);
    
        if (!isInEditMode()) {
            getLegend().setTypeface(TypeFace.INSTANCE.getMediumTypeFace(getContext()));
        }
    
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
    
    public void startAnimation() {
        if (!AccessibilityPreferences.INSTANCE.isAnimationReduced()) {
            animateXY(1000, 500, Easing.EaseOutCubic);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //noinspection SwitchStatementWithTooFewBranches
        switch (key) {
            case AnalyticsPreferences.pieHoleRadius -> {
                animateHoleRadius(AnalyticsPreferences.INSTANCE.getPieHoleRadiusValue());
            }
        }
    }
    
    @Override
    public void onThemeChanged(@NonNull Theme theme, boolean animate) {
        initProps();
        invalidate();
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            app.simple.inure.preferences.SharedPreferences.INSTANCE
                    .getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }
        ThemeManager.INSTANCE.addListener(this);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        app.simple.inure.preferences.SharedPreferences.INSTANCE
                .getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        ThemeManager.INSTANCE.removeListener(this);
        if (NullSafety.INSTANCE.isNotNull(valueAnimator)) {
            valueAnimator.cancel();
        }
        clearAnimation();
    }
    
    private void animateHoleRadius(float value) {
        if (animate) {
            if (NullSafety.INSTANCE.isNotNull(valueAnimator)) {
                valueAnimator.cancel();
            }
            valueAnimator = ValueAnimator.ofFloat(getHoleRadius(), value);
            valueAnimator.setDuration(getResources().getInteger(R.integer.animation_duration));
            valueAnimator.setInterpolator(Utils.getInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                setHoleRadius((float) animation.getAnimatedValue());
                invalidate();
            });
            valueAnimator.start();
        } else {
            setHoleRadius(value);
            invalidate();
        }
    }
    
    /**
     * Should animate the chart when data changes
     * and when the chart is first drawn.
     * This flag should also affect the hole radius animation
     *
     * @param animate true to animate the chart
     */
    public void setAnimation(boolean animate) {
        this.animate = animate;
    }
}