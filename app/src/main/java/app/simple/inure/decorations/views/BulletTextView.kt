package app.simple.inure.decorations.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.util.NullSafety.isNotNull
import kotlin.math.abs

class BulletTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TypeFaceTextView(context, attrs) {

    private var bulletDrawable: Drawable? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setBulletPoint(drawableLeft = bulletDrawable, canvas)
    }

    private fun setBulletPoint(drawableLeft: Drawable?, canvas: Canvas?) {
        if (!text.isNullOrEmpty()) {
            if (bulletDrawable.isNotNull()) {
                drawableLeft?.let {
                    if (lineCount == 1) {
                        setCompoundDrawablesWithIntrinsicBounds(it, null, null, null)
                    } else {
                        val buttonWidth = it.intrinsicWidth
                        val buttonHeight = it.intrinsicHeight
                        val topSpace = abs(buttonHeight - lineHeight)

                        setPadding(buttonWidth + compoundDrawablePadding, 0, 0, 0)

                        it.setBounds(0, topSpace, buttonWidth, topSpace + buttonHeight)

                        canvas?.apply {
                            save()
                            it.draw(canvas)
                            restore()
                        }
                    }
                }
            } else {
                setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                setPadding(0, 0, 0, 0)
            }
        }
    }

    fun setBulletDrawable(drawable: Drawable) {
        bulletDrawable = drawable
        bulletDrawable!!.setTint(getDrawableTintColor())
        setBulletPoint(drawableLeft = bulletDrawable, canvas = Canvas())
    }

    fun removeBulletDrawable() {
        bulletDrawable = null
        setBulletPoint(drawableLeft = null, canvas = Canvas())
    }
}