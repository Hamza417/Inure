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
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.DevelopmentPreferences
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class AlbumArtAdapter(val list: ArrayList<AudioModel>) : RecyclerView.Adapter<AlbumArtAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_album_art, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.albumArt.transitionName = list[position].fileUri

        if (DevelopmentPreferences.get(DevelopmentPreferences.loadAlbumArtFromFile)) {
            holder.albumArt.loadFromFileDescriptor(list[position].fileUri.toUri())
        } else {
            holder.albumArt.scaleType = ImageView.ScaleType.CENTER_CROP
            holder.albumArt.setImageURI(list[position].artUri.toUri())
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
        GlideApp.with(this)
            .asBitmap()
            .dontAnimate()
            .transform(CenterCrop())
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptor.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptor.drawable as AnimatedVectorDrawable).start()
                    }
                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    return false
                }
            })
            .into(this)
    }
}