package app.simple.inure.dialogs.appearance

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.graphics.drawable.toDrawable
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.glide.transformation.BlurShadow
import app.simple.inure.glide.transformation.Padding
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class IconSize : ScopedBottomSheetFragment() {

    private lateinit var iconPreview: ImageView
    private lateinit var seekbar: ThemeSeekBar
    private lateinit var set: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var iconPlaceholder: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_icon_resize, container, false)

        iconPreview = view.findViewById(R.id.resized_icon_preview)
        seekbar = view.findViewById(R.id.icon_size_seekbar)
        set = view.findViewById(R.id.set)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setImage(AppearancePreferences.getIconSize())
        seekbar.progress = AppearancePreferences.getIconSize()

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (progress < 50) {
                        setImage(50)
                    } else {
                        setImage(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })

        set.setOnClickListener {
            AppearancePreferences.setIconSize(if (seekbar.progress < 50) 50 else seekbar.progress)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setImage(size: Int) {
        Glide.with(iconPreview).clear(iconPreview).also {
            Glide.with(iconPreview)
                .asBitmap()
                .placeholder(iconPlaceholder?.toDrawable(resources))
                .transform(
                        Padding(BlurShadow.MAX_BLUR_RADIUS.toInt()),
                        BlurShadow(requireContext())
                            .setElevation(25F)
                            .setBlurRadius(BlurShadow.MAX_BLUR_RADIUS))
                .addListener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        iconPlaceholder = resource
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                })
                .load(R.mipmap.ic_launcher.toBitmap(requireContext(), size))
                .into(iconPreview)
        }
    }

    companion object {
        fun newInstance(): IconSize {
            val args = Bundle()
            val fragment = IconSize()
            fragment.arguments = args
            return fragment
        }
    }
}