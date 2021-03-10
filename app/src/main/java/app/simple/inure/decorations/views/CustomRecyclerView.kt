package app.simple.inure.decorations.views

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.decorations.bouncescroll.RecyclerViewVerticalElasticScroll.setupEdgeEffectFactory
import app.simple.inure.decorations.viewholders.VerticalListViewHolder
import app.simple.inure.util.StatusBarHeight

class CustomRecyclerView(context: Context, attrs: AttributeSet?) :
    RecyclerView(context, attrs) {

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)
        setupEdgeEffectFactory<VerticalListViewHolder>()
    }
}
