package app.simple.inure.decorations.indicatorfastscroll

import androidx.recyclerview.widget.RecyclerView

internal open class ItemIndicatorsBuilder {

    open fun buildItemIndicators(recyclerView: RecyclerView, getItemIndicator: (Int) -> FastScrollItemIndicator?, showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)?): List<ItemIndicatorWithPosition> {

        return (0 until recyclerView.adapter!!.itemCount).mapNotNull { position ->
            getItemIndicator(position)?.let { ItemIndicatorWithPosition(it, position) }

        }.distinctBy(ItemIndicatorWithPosition::first).let { unfilteredIndicators ->
            showIndicator?.let {
                unfilteredIndicators.filterIndexed { index, (indicator, _) ->
                    it(indicator, index, unfilteredIndicators.size)
                }
            } ?: unfilteredIndicators
        }
    }
}
