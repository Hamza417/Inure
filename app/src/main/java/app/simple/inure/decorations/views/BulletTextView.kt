package app.simple.inure.decorations.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import app.simple.inure.decorations.typeface.TypeFaceTextView
import kotlin.math.abs

class BulletTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TypeFaceTextView(context, attrs) {

    private var bulletDrawable: Drawable? = null

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        setBulletPoint(drawableLeft = bulletDrawable, canvas)
    }

    private fun setBulletPoint(drawableLeft: Drawable?, canvas: Canvas?) {
        if (!TextUtils.isEmpty(text)) {
            drawableLeft?.let { it ->
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
        }
    }

    fun setBulletDrawable(drawable: Drawable) {
        bulletDrawable = drawable
        bulletDrawable!!.setTint(getDrawableTintColor())
        setBulletPoint(drawableLeft = bulletDrawable, canvas = Canvas())
    }
}