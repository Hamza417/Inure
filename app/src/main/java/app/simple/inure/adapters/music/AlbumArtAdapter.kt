package app.simple.inure.adapters.music

import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import app.simple.inure.glide.transformation.Blur
import app.simple.inure.glide.uricover.UriCoverModel
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.DevelopmentPreferences
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AlbumArtAdapter(val list: ArrayList<AudioModel>) : RecyclerView.Adapter<AlbumArtAdapter.Holder>() {

    var onAlbumArtClicked: ((ImageView, Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_album_art, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.albumArt.transitionName = list[position].fileUri

        if (DevelopmentPreferences.get(DevelopmentPreferences.USE_ALTERNATE_AUDIO_PLAYER_INTERFACE)) {
            if (DevelopmentPreferences.get(DevelopmentPreferences.LOAD_ALBUM_ART_FROM_FILE)) {
                holder.albumArt.loadBlurredBackgroundDescriptor(list[position].fileUri.toUri())
            } else {
                holder.albumArt.loadBlurredBackground(list[position].artUri.toUri())
            }
        } else {
            if (DevelopmentPreferences.get(DevelopmentPreferences.LOAD_ALBUM_ART_FROM_FILE)) {
                holder.albumArt.loadFromFileDescriptor(list[position].fileUri.toUri())
            } else {
                holder.albumArt.loadFromUri(list[position].artUri.toUri())
            }
        }

        holder.albumArt.setOnClickListener {
            onAlbumArtClicked?.invoke(holder.albumArt, position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val albumArt: ImageView = itemView.findViewById(R.id.album_art)
    }

    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptor(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .dontAnimate()
            .transform(CenterCrop())
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptor.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptor.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }

    fun ImageView.loadFromUri(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .dontAnimate()
            .transform(CenterCrop())
            .load(UriCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                    this@loadFromUri.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromUri.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap, model: Any, target: Target<Bitmap>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }

    fun ImageView.loadBlurredBackgroundDescriptor(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .transition(BitmapTransitionOptions.withCrossFade())
            .transform(CenterCrop(), Blur(25))
            .load(DescriptorCoverModel(this.context, uri))
            .into(this)
    }

    fun ImageView.loadBlurredBackground(uri: Uri) {
        Glide.with(this)
            .asBitmap()
            .transition(BitmapTransitionOptions.withCrossFade())
            .transform(CenterCrop(), Blur(25))
            .load(UriCoverModel(this.context, uri))
            .into(this)
    }
}
