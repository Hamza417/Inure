package app.simple.inure.decorations.overscroll

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import androidx.core.view.children
import app.simple.inure.preferences.RecyclerViewPreferences
import kotlin.math.min

class ScalingRecyclerView(context: Context, attrs: AttributeSet?) : CustomVerticalRecyclerView(context, attrs), ScaleGestureDetector.OnScaleGestureListener {

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var scaleFactor = RecyclerViewPreferences.getViewScaleFactor()

    init {
        scaleGestureDetector = ScaleGestureDetector(context, this)
        setHasFixedSize(false)
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        Log.d("CustomVerticalRecyclerView", "onScale: ${detector.scaleFactor}")
        kotlin.runCatching {
            for (itemPosition in 0 until adapter?.itemCount!!) {
                for (view in (layoutManager?.findViewByPosition(itemPosition) as? ViewGroup?)!!.children) {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = 0.1f.coerceAtLeast(min(scaleFactor, 10.0f))
                    view.scaleX = detector.scaleFactor
                    view.scaleY = detector.scaleFactor
                    view.invalidate()
                    RecyclerViewPreferences.setViewScaleFactor(scaleFactor)
                }
            }
        }.getOrElse {
            it.printStackTrace()
        }

        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        return true
        /* no-op */
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        /* no-op */
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(e)
        return super.onTouchEvent(e)
    }
}