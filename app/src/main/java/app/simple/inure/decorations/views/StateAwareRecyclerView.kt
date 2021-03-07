package app.simple.inure.decorations.views

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.adapters.AppsAdapter
import app.simple.inure.decorations.bouncescroll.RecyclerViewVerticalElasticScroll.setupEdgeEffectFactory
import app.simple.inure.util.NullSafety.isNotNull

class StateAwareRecyclerView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs) {

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setHasFixedSize(true)
        setupEdgeEffectFactory<AppsAdapter.Holder>()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()

        if (layoutManager.isNotNull() && layoutManager is LinearLayoutManager) {
            scrollPosition = (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        }

        return superState
    }

    companion object {
        private var scrollPosition = 0
    }
}