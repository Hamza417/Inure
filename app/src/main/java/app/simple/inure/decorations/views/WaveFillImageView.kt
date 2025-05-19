package app.simple.inure.decorations.views

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.themes.manager.ThemeManager
import kotlin.math.sin

class WaveFillImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null
) : SquareImageView(context, attrs) {

    private var unfilledColor: Int = Color.LTGRAY                   // faint gray for skeleton image
    private var waveColor: Int = Color.BLUE                         // default wave fill color
    private var waveCount: Int = 2                                  // number of sine cycles visible
    private var waveAmplitude: Float = 20f                          // height of the wave

    private val wavePath = Path()
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var offset = 0f
    private var fillPercent = 0f

    private var animator: ValueAnimator? = null
    private var fillAnimator: ValueAnimator? = null
    private var amplitudeAnimator: ValueAnimator? = null
    private var pollingAnimator: AnimatorSet? = null

    init {
        maskPaint.colorFilter = PorterDuffColorFilter(unfilledColor, PorterDuff.Mode.SRC_IN)
        wavePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        wavePaint.color = waveColor
        startAnimation()
    }

    fun setFillPercent(percent: Float, animate: Boolean = true) {
        val target = percent.coerceIn(0f, 1f)
        if (!animate) {
            fillPercent = target
            invalidate()
            return
        }
        fillAnimator?.cancel()
        fillAnimator = ValueAnimator.ofFloat(fillPercent, target).apply {
            duration = 600
            interpolator = LinearInterpolator()
            addUpdateListener {
                fillPercent = it.animatedValue as Float
                invalidate()
            }
            start()
        }

        setWaveAmplitude(waveAmplitude)
    }

    fun setWaveColor(color: Int) {
        waveColor = color
        wavePaint.color = color
        wavePaint.shader = null
        invalidate()
    }

    fun setUnfilledColor(color: Int) {
        unfilledColor = color
        imageTintList = ColorStateList.valueOf(color)
        invalidate()
    }

    fun setWaveCount(count: Int) {
        waveCount = count.coerceAtLeast(1)
        invalidate()
    }

    fun setWaveAmplitude(amplitude: Float, animate: Boolean = true) {
        if (waveAmplitude != amplitude) {
            if (animate) {
                amplitudeAnimator?.cancel()
                amplitudeAnimator = ValueAnimator.ofFloat(waveAmplitude, amplitude).apply {
                    duration = 600
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        waveAmplitude = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            } else {
                waveAmplitude = amplitude
                invalidate()
            }
        }
    }

    fun startPollingWave(spikeAmplitude: Float = 60f, spikeDuration: Long = 400L, settleDuration: Long = 2500L) {
        pollingAnimator?.cancel()
        val originalAmplitude = waveAmplitude

        // Spike up
        val spikeAnimator = ValueAnimator.ofFloat(waveAmplitude, spikeAmplitude).apply {
            this.duration = spikeDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                setWaveAmplitude(it.animatedValue as Float, false)
            }
        }

        // Return to normal
        val returnAnimator = ValueAnimator.ofFloat(spikeAmplitude, originalAmplitude).apply {
            this.duration = settleDuration
            interpolator = LinearInterpolator()
            addUpdateListener {
                setWaveAmplitude(it.animatedValue as Float, false)
            }
        }

        pollingAnimator = AnimatorSet().apply {
            playSequentially(spikeAnimator, returnAnimator)
            start()
        }
    }

    private fun buildWavePath(path: Path, waveY: Float) {
        path.reset()
        path.moveTo(0f, height.toFloat())

        val w = width.toFloat()

        var x = 0f
        while (x <= w + 1) {
            val angle = 2 * Math.PI * waveCount * (x + offset) / w
            val y = (waveAmplitude * sin(angle)).toFloat()
            path.lineTo(x, waveY + y)
            x += 1f
        }

        path.lineTo(w, height.toFloat())
        path.close()
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2500
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val progress = it.animatedFraction
                offset = width * progress
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        pollingAnimator?.cancel()
        amplitudeAnimator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        val waveY = height.toFloat() * (1f - fillPercent)

        val save1 = canvas.saveLayer(null, null)
        super.onDraw(canvas)
        canvas.drawColor(unfilledColor, PorterDuff.Mode.SRC_IN)
        canvas.restoreToCount(save1)

        val save2 = canvas.saveLayer(null, null)
        super.onDraw(canvas)

        buildWavePath(wavePath, waveY)
        canvas.drawPath(wavePath, wavePaint)

        canvas.restoreToCount(save2)
    }

    fun setSimpleDefaults() {
        setFillPercent(0.6F, false)
        setWaveColor(AppearancePreferences.getAccentColor())
        setWaveAmplitude(5F, false)
        setUnfilledColor(ThemeManager.theme.switchViewTheme.switchOffColor)
    }
}


