package app.simple.inure.decorations.views

import android.content.Context
import android.graphics.*
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import app.simple.inure.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class IconView @JvmOverloads constructor(context: Context, attrs: AttributeSet, @AttrRes defStyleAttr: Int = R.attr.iconViewStyle, @StyleRes defStyleRes: Int = R.style.Widget_ShadowPlay_IconView)
    : View(context, attrs, defStyleAttr, defStyleRes), CoroutineScope {

    var icon: Drawable? = null
        set(value) {
            field = value
            setIconBounds()
            createShadows()
        }

    var offset = 0f
        set(value) {
            field = fastOutSlowInInterpolator.getInterpolation(value.coerceIn(0f, 1f))
            val scale = scaleDown + (1f - scaleDown) * (1f - field)
            scaleX = scale
            scaleY = scale
            smallBlurShadowAlpha = (field * 255).toInt()
            bigBlurShadowAlpha = 255 - smallBlurShadowAlpha
            shadowTranslate = constantShadowTranslationY + variableShadowTranslationY * (1f - field)
            postInvalidateOnAnimation()
        }

    private var bigBlurShadow: Bitmap? = null
    private var smallBlurShadow: Bitmap? = null
    private var smallBlurShadowAlpha = 255
    private var bigBlurShadowAlpha = 255
    private var shadowTranslate = 0f
    private var constantShadowTranslationY = 8f
    private var variableShadowTranslationY = 16f
    private var scaleDown = 0.85f
    private var bigBlurRadius = 24f
    private var smallBlurRadius = 8f
    private var padding = bigBlurRadius.toInt()
    private val iconBounds = Rect()
    private val shadowBounds = RectF()
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setScale(SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_RGB, SHADOW_SCALE_ALPHA)
        })
    }

    init {
        val a =
            context.obtainStyledAttributes(attrs, R.styleable.IconView, defStyleAttr, defStyleRes)
        constantShadowTranslationY =
            a.getDimension(R.styleable.IconView_constantShadowTranslationY, constantShadowTranslationY)
        variableShadowTranslationY =
            a.getDimension(R.styleable.IconView_variableShadowTranslationY, variableShadowTranslationY)
        scaleDown = a.getFloat(R.styleable.IconView_scaleDown, scaleDown).coerceIn(0f, 1f)
        bigBlurRadius =
            a.getFloat(R.styleable.IconView_bigBlurRadius, bigBlurRadius).coerceIn(0f, 25f)
        padding = bigBlurRadius.toInt()
        smallBlurRadius =
            a.getFloat(R.styleable.IconView_smallBlurRadius, smallBlurRadius).coerceIn(0f, 25f)
        a.recycle()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val shadowTop = (h - w) / 2
        val iconTop = shadowTop + padding
        val iconSize = w - padding - padding
        iconBounds.set(padding, iconTop, padding + iconSize, iconTop + iconSize)
        shadowBounds.set(0f, shadowTop.toFloat(), w.toFloat(), (shadowTop + w).toFloat())
        setIconBounds()
        createShadows()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val saveCount = canvas.save()
        canvas.translate(shadowBounds.left, shadowBounds.top + shadowTranslate)
        shadowPaint.alpha = bigBlurShadowAlpha
        canvas.drawBitmap(bigBlurShadow!!, 0f, 0f, shadowPaint)
        shadowPaint.alpha = smallBlurShadowAlpha
        canvas.drawBitmap(smallBlurShadow!!, 0f, 0f, shadowPaint)
        canvas.restoreToCount(saveCount)
        icon?.draw(canvas)
    }

    private fun setIconBounds() {
        icon?.bounds = iconBounds
    }

    private fun createShadows() {
        if (icon == null || shadowBounds.width() == 0f) return
        if (bigBlurShadow == null) {
            bigBlurShadow = Bitmap.createBitmap(shadowBounds.width().toInt(), shadowBounds.height()
                .toInt(), ARGB_8888)
        } else {
            bigBlurShadow?.eraseColor(Color.TRANSPARENT)
        }
        if (smallBlurShadow == null) {
            smallBlurShadow =
                Bitmap.createBitmap(shadowBounds.width().toInt(), shadowBounds.height()
                    .toInt(), ARGB_8888)
        } else {
            smallBlurShadow?.eraseColor(Color.TRANSPARENT)
        }
        val c = Canvas()
        createShadow(bigBlurShadow, c, bigBlurRadius)
        createShadow(smallBlurShadow, c, smallBlurRadius)
    }

    private fun createShadow(bitmap: Bitmap?, canvas: Canvas, blurRadius: Float) {
        canvas.setBitmap(bitmap)
        canvas.translate(padding - iconBounds.left.toFloat(), padding - iconBounds.top.toFloat())
        icon?.draw(canvas)
        val rs = getRS(context)
        val blur = getBlur(context)
        val input = Allocation.createFromBitmap(rs, bitmap)
        val output = Allocation.createTyped(rs, input.type)
        blur.setRadius(blurRadius.coerceIn(0f, 25f))
        blur.setInput(input)
        blur.forEach(output)
        output.copyTo(bitmap)
        input.destroy()
        output.destroy()
    }

    companion object {
        private const val SHADOW_SCALE_RGB = 0.85f
        private const val SHADOW_SCALE_ALPHA = 0.6f
        private val fastOutSlowInInterpolator = FastOutSlowInInterpolator()

        private var rs: RenderScript? = null
        fun getRS(context: Context): RenderScript {
            if (rs == null) {
                rs = RenderScript.create(context.applicationContext)
            }
            return rs!!
        }

        private var blur: ScriptIntrinsicBlur? = null
        fun getBlur(context: Context): ScriptIntrinsicBlur {
            if (blur == null) {
                val rs = getRS(context)
                blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            }
            return blur!!
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main
}
