package app.simple.inure.decorations.views;

import android.content.Context;
import android.content.res.ColorStateList;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;

import app.simple.inure.R;
import app.simple.inure.decorations.corners.LayoutBackground;
import app.simple.inure.decorations.typeface.TypeFaceTextView;
import app.simple.inure.util.ColorUtils;
import app.simple.inure.util.ViewUtils;

public class ChartMarkerView extends MarkerView {
    
    private TypeFaceTextView textData;
    
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public ChartMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        init();
    }
    
    private void init() {
        textData = findViewById(R.id.chart_text_data);
        setElevation(getResources().getDimensionPixelOffset(R.dimen.app_views_elevation));
        setBackgroundTintList(ColorStateList.valueOf(ColorUtils.INSTANCE.resolveAttrColor(getContext(), R.attr.colorAppAccent)));
        LayoutBackground.setBackground(this);
        ViewUtils.INSTANCE.addShadow(this);
        setPadding(20, 10, 20, 10);
    }
    
    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        textData.setText(((PieEntry) e).getLabel());
        super.refreshContent(e, highlight);
    }
}