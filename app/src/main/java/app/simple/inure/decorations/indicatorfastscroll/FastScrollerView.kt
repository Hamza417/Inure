package app.simple.inure.decorations.indicatorfastscroll

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.*
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.layoutmanager.stack.StackLayoutManager
import app.simple.inure.decorations.layoutmanager.vega.VegaLayoutManager
import kotlin.math.abs
import kotlin.math.min


typealias ItemIndicatorWithPosition = Pair<FastScrollItemIndicator, Int>

/**
 * A fast scroller that observes a [RecyclerView]'s data and presents its items in a vertical column
 * of [text][FastScrollItemIndicator.Text] or [icon][FastScrollItemIndicator.Icon] indicators. It
 * also optionally handles scrolling to their respective items. It can be placed independently of
 * the RecyclerView, and has no layout dependencies.
 *
 * @see setupWithRecyclerView
 * @see FastScrollerThumbView
 */
class FastScrollerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.indicatorFastScrollerStyle, defStyleRes: Int = R.style.Widget_IndicatorFastScroll_FastScroller) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {

    var iconSize: Int = 0
        set(value) {
            field = value
            bindItemIndicatorViews()
        }
    var iconColor: ColorStateList? = null
        set(value) {
            field = value
            pressedIconColor = value?.getColorForState(intArrayOf(android.R.attr.state_activated))
            bindItemIndicatorViews()
        }
    var textAppearanceRes: Int = 0
        set(value) {
            field = value
            bindItemIndicatorViews()
        }
    var textColor: ColorStateList? = null
        set(value) {
            field = value
            pressedTextColor = value?.getColorForState(intArrayOf(android.R.attr.state_activated))
            bindItemIndicatorViews()
        }
    var textPadding: Float = 0f
        set(value) {
            field = value
            bindItemIndicatorViews()
        }

    private var pressedIconColor: Int? = null
    private var pressedTextColor: Int? = null

    internal var itemIndicatorsBuilder: ItemIndicatorsBuilder = ItemIndicatorsBuilder()

    val itemIndicatorSelectedCallbacks: MutableList<ItemIndicatorSelectedCallback> = ArrayList()

    internal var onItemIndicatorTouched: ((Boolean) -> Unit)? = null

    val isSetup: Boolean get() = (recyclerView != null)
    private var recyclerView: RecyclerView? = null
    private var adapter: RecyclerView.Adapter<*>? = null
        set(value) {
            field?.unregisterAdapterDataObserver(adapterDataObserver)
            field = value
            value?.let { newAdapter ->
                newAdapter.registerAdapterDataObserver(adapterDataObserver)
                postUpdateItemIndicators()
            }
        }
    private val adapterDataObserver: RecyclerView.AdapterDataObserver = createAdapterDataObserver()
    private lateinit var getItemIndicator: (Int) -> FastScrollItemIndicator?

    /**
     * An optional predicate for deciding which indicators to show after they have been computed.
     * The first parameter is the subject indicator.
     * The second parameter is its position in the [list of indicators][itemIndicators].
     * The third parameter is the total number of computed indicators, including ones that have been
     * filtered out via this predicate.
     * The function will be called when building the list of indicators, which happens after the
     * RecyclerView's adapter's data changes. It will be called on the UI thread.
     */
    var showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)? by onUpdate { _ ->
        postUpdateItemIndicators()
    }

    /**
     * Whether or not the RecyclerView will be automatically scrolled when an indicator is pressed.
     * Set to false if you'd rather handle the scrolling yourself by adding a
     * [ItemIndicatorSelectedCallback].
     *
     * @see scrollToPosition
     */
    var useDefaultScroller: Boolean = true
    private var lastSelectedPosition: Int? = null
    private var isUpdateItemIndicatorsPosted = false

    private val itemIndicatorsWithPositions: MutableList<ItemIndicatorWithPosition> = ArrayList()

    /**
     * The list of indicators being shown. This will contain no duplicates, and will be built with
     * respect to the iteration order of the RecyclerView's adapter's data.
     */
    val itemIndicators: List<FastScrollItemIndicator>
        get() = itemIndicatorsWithPositions.map(ItemIndicatorWithPosition::first)

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.FastScrollerView, defStyleAttr, defStyleRes)
                .use { attrsArray ->
                    throwIfMissingAttrs(styleRes = R.style.Widget_IndicatorFastScroll_FastScroller) {
                        iconSize = attrsArray.getDimensionPixelSizeOrThrow(R.styleable.FastScrollerView_fastScrollerIconSize)
                        iconColor = attrsArray.getColorStateListOrThrow(R.styleable.FastScrollerView_fastScrollerIconColor)
                        textAppearanceRes = attrsArray.getResourceIdOrThrow(R.styleable.FastScrollerView_android_textAppearance)
                        textColor = attrsArray.getColorStateListOrThrow(R.styleable.FastScrollerView_android_textColor)
                        textPadding = attrsArray.getDimensionOrThrow(R.styleable.FastScrollerView_fastScrollerTextPadding)
                    }
                }

        isFocusableInTouchMode = true
        isClickable = true
        orientation = VERTICAL
        gravity = Gravity.CENTER
        alpha = 0F

        if (isInEditMode) {
            itemIndicatorsWithPositions += listOf(ItemIndicatorWithPosition(FastScrollItemIndicator.Text("A"), 0), ItemIndicatorWithPosition(FastScrollItemIndicator.Text("B"), 1), ItemIndicatorWithPosition(FastScrollItemIndicator.Text("C"), 2), ItemIndicatorWithPosition(FastScrollItemIndicator.Text("D"), 3), ItemIndicatorWithPosition(FastScrollItemIndicator.Text("E"), 4))
            bindItemIndicatorViews()
        }
    }

    /**
     * Sets up this [FastScrollerView] to present item indicators for [recyclerView]'s data.
     * The data is observed through its adapter, and each item is (optionally) mapped to an indicator
     * with [getItemIndicator]. After calling one of the adapter's notify methods,
     * [the list of indicators][itemIndicators] will be built and presented.
     * The indicators can optionally be filtered with [showIndicator].
     * Only call this function once.
     *
     * @param recyclerView the [RecyclerView] whose data's indicators will be presented.
     * @param getItemIndicator a function mapping an item position to a [FastScrollItemIndicator].
     *                         This will be called when building the list of indicators, which happens
     *                         immediately as well as whenever the adapter's data changes. If items
     *                         return identical indicators, they will be merged and only shown once.
     *                         To not show an indicator for an item, return null.
     *                         Called on the UI thread.
     * @param showIndicator an optional predicate for filtering indicators. This can be changed
     *                      (or removed) at any time.
     *                      See [FastScrollerView.showIndicator].
     * @param useDefaultScroller whether or not this FastScrollerView should automatically scroll
     *                           [recyclerView] when an indicator is pressed.
     *                           See [FastScrollerView.useDefaultScroller].
     */
    @JvmOverloads
    fun setupWithRecyclerView(recyclerView: RecyclerView, getItemIndicator: (Int) -> FastScrollItemIndicator?, showIndicator: ((FastScrollItemIndicator, Int, Int) -> Boolean)? = null, useDefaultScroller: Boolean = true) {
        check(!isSetup) { "Only set this view's RecyclerView once!" }
        this.recyclerView = recyclerView
        this.getItemIndicator = getItemIndicator
        this.showIndicator = showIndicator
        this.useDefaultScroller = useDefaultScroller

        this.adapter = recyclerView.adapter.also {
            if (it != null) {
                updateItemIndicators()
            }
        }
        recyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            // RecyclerView#setAdapter calls requestLayout, so this can detect adapter changes
            if (recyclerView.adapter !== adapter) {
                this@FastScrollerView.adapter = recyclerView.adapter
            }
        }
    }

    private fun postUpdateItemIndicators() {
        if (!isUpdateItemIndicatorsPosted) {
            isUpdateItemIndicatorsPosted = true
            post {
                if (recyclerView!!.run { isAttachedToWindow && adapter != null }) {
                    updateItemIndicators()
                }
                isUpdateItemIndicatorsPosted = false
            }
        }
    }

    private fun updateItemIndicators() {
        itemIndicatorsWithPositions.clear()
        itemIndicatorsBuilder.buildItemIndicators(recyclerView!!, getItemIndicator, showIndicator)
                .toCollection(itemIndicatorsWithPositions)

        bindItemIndicatorViews()
    }

    private fun bindItemIndicatorViews() {
        removeAllViews()

        if (itemIndicatorsWithPositions.isEmpty()) {
            return
        }

        fun createIconView(iconIndicator: FastScrollItemIndicator.Icon): ImageView =
            (LayoutInflater.from(context)
                    .inflate(R.layout.fast_scroller_indicator_icon, this, false) as ImageView).apply {
                updateLayoutParams {
                    width = iconSize
                    height = iconSize
                }
                iconColor?.let(::setImageTintList)
                setImageResource(iconIndicator.iconRes)
                tag = iconIndicator
            }

        fun createTextView(textIndicators: List<FastScrollItemIndicator.Text>): TextView =
            (LayoutInflater.from(context)
                    .inflate(R.layout.fast_scroller_indicator_text, this, false) as TextView).apply {
                TextViewCompat.setTextAppearance(this, textAppearanceRes)
                textColor?.let(::setTextColor)
                updatePadding(top = textPadding.toInt(), bottom = textPadding.toInt())
                setLineSpacing(textPadding, lineSpacingMultiplier)
                text = textIndicators.joinToString(separator = "\n") { it.text }
                tag = textIndicators
            }

        // Optimize the views by batching adjacent text indicators into a single TextView
        val views = ArrayList<View>()
        itemIndicators.run {
            var index = 0
            while (index <= lastIndex) {
                @Suppress("UNCHECKED_CAST") val textIndicatorsBatch = subList(index, size).takeWhile { it is FastScrollItemIndicator.Text } as List<FastScrollItemIndicator.Text>
                if (textIndicatorsBatch.isNotEmpty()) {
                    views.add(createTextView(textIndicatorsBatch))
                    index += textIndicatorsBatch.size
                } else {
                    when (val indicator = this[index]) {
                        is FastScrollItemIndicator.Icon -> {
                            views.add(createIconView(indicator))
                        }
                        is FastScrollItemIndicator.Text -> {
                            throw IllegalStateException("Text indicator wasn't batched")
                        }
                    }
                    index++
                }
            }
        }
        views.forEach(::addView)
    }

    private fun selectItemIndicator(indicator: FastScrollItemIndicator, indicatorCenterY: Int, touchedView: View, textLine: Int?) {
        val position = itemIndicatorsWithPositions.first { it.first == indicator }
                .let(ItemIndicatorWithPosition::second)
        if (position != lastSelectedPosition) {
            clearSelectedItemIndicator()
            lastSelectedPosition = position
            if (useDefaultScroller) {
                scrollToPosition(position)
            }
            performHapticFeedback(
                // Semantically, dragging across the indicators is similar to moving a text handle
                if (Build.VERSION.SDK_INT >= 27) {
                    HapticFeedbackConstants.TEXT_HANDLE_MOVE
                } else {
                    HapticFeedbackConstants.KEYBOARD_TAP
                })
            if (touchedView is ImageView) {
                touchedView.isActivated = true
            } else if (textLine != null) {
                pressedTextColor?.let { color ->
                    TextColorUtil.highlightAtIndex(touchedView as TextView, textLine, color)
                }
            }
            itemIndicatorSelectedCallbacks.forEach {
                it.onItemIndicatorSelected(indicator, indicatorCenterY, position)
            }
        }
    }

    private fun clearSelectedItemIndicator() {
        lastSelectedPosition = null
        if (pressedIconColor != null) {
            children.filterIsInstance<ImageView>().forEach { it.isActivated = false }
        }
        if (pressedTextColor != null) {
            children.filterIsInstance<TextView>().forEach(TextColorUtil::clearHighlight)
        }
    }

    private fun scrollToPosition(position: Int) {
        recyclerView!!.apply {
            stopScroll()

            when (this.layoutManager) {
                is StackLayoutManager -> {
                    (layoutManager as StackLayoutManager).scrollToPosition(position)
                }
                is LinearLayoutManager -> {
                    val layoutManager = this.layoutManager as LinearLayoutManager
                    layoutManager.scrollToPositionWithOffset(
                        position,
                        abs(
                            layoutManager.findLastCompletelyVisibleItemPosition()
                                    - layoutManager.findFirstCompletelyVisibleItemPosition()))
                }
                is VegaLayoutManager -> {
                    (layoutManager as VegaLayoutManager).scrollToPosition(position)
                }
            }

            /**
             * [RecyclerView.smoothScrollToPosition] is not suitable
             * for long lists, it will take a long time to scroll
             * to positions
             */
            //smoothScrollToPosition(position)
            //scrollToPosition(position)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        clearAnimation()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animate().alpha(1F).setInterpolator(DecelerateInterpolator()).start()
            }
            MotionEvent.ACTION_UP -> {
                animate().alpha(0F).setInterpolator(DecelerateInterpolator()).start()
            }
        }

        fun View.containsY(y: Int) = y in (top until bottom)

        if (event.actionMasked in MOTION_EVENT_STOP_ACTIONS) {
            isPressed = false
            clearSelectedItemIndicator()
            onItemIndicatorTouched?.invoke(false)
            return false
        }

        var consumed = false
        val touchY = event.y.toInt()
        children.forEach { view ->
            if (view.containsY(touchY)) {
                when (view) {
                    is ImageView -> {
                        val touchedIndicator = view.tag as FastScrollItemIndicator.Icon
                        val centerY = view.y.toInt() + (view.height / 2)
                        selectItemIndicator(touchedIndicator, centerY, view, textLine = null)
                        consumed = true
                    }
                    is TextView -> {
                        @Suppress("UNCHECKED_CAST") val possibleTouchedIndicators = view.tag as List<FastScrollItemIndicator.Text>
                        val textIndicatorsTouchY = touchY - view.top
                        val textLineHeight = view.height / possibleTouchedIndicators.size
                        val touchedIndicatorIndex = min(textIndicatorsTouchY / textLineHeight, possibleTouchedIndicators.lastIndex)
                        val touchedIndicator = possibleTouchedIndicators[touchedIndicatorIndex]

                        val centerY = view.y.toInt() + (textLineHeight / 2) + (touchedIndicatorIndex * textLineHeight)
                        selectItemIndicator(touchedIndicator, centerY, view, textLine = touchedIndicatorIndex)
                        consumed = true
                    }
                }
            }
        }

        isPressed = consumed

        onItemIndicatorTouched?.invoke(consumed)
        return consumed
    }

    companion object {

        private val MOTION_EVENT_STOP_ACTIONS = intArrayOf(MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL)

        private fun FastScrollerView.createAdapterDataObserver(): RecyclerView.AdapterDataObserver {
            return object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    postUpdateItemIndicators()
                }

                override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
                    onChanged()

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = onChanged()

                override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) =
                    onChanged()

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = onChanged()
            }
        }
    }

    interface ItemIndicatorSelectedCallback {
        fun onItemIndicatorSelected(indicator: FastScrollItemIndicator, indicatorCenterY: Int, itemPosition: Int)
    }
}
