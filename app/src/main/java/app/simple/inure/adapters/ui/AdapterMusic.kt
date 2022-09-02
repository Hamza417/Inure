package app.simple.inure.adapters.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.RecyclerViewConstants
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.AudioCoverUtil.loadFromUri
import app.simple.inure.models.AudioModel

class AdapterMusic(val list: ArrayList<AudioModel>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var musicCallbacks: MusicCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return when (viewType) {
            RecyclerViewConstants.TYPE_HEADER -> {
                Header(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_header_music, parent, false))
            }
            RecyclerViewConstants.TYPE_ITEM -> {
                Holder(LayoutInflater.from(parent.context)
                           .inflate(R.layout.adapter_music, parent, false))
            }
            else -> {
                throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = position_ - 1

        if (holder is Holder) {
            holder.title.text = list[position].title
            holder.artists.text = list[position].artists
            holder.album.text = list[position].album

            holder.art.loadFromUri(holder.context, Uri.parse(list[position].artUri))

            holder.container.setOnClickListener {
                musicCallbacks?.onMusicClicked(Uri.parse(list[position].fileUri))
            }
        } else if (holder is Header) {

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            RecyclerViewConstants.TYPE_HEADER
        } else RecyclerViewConstants.TYPE_ITEM
    }

    override fun getItemCount(): Int {
        return list.size.plus(1)
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            GlideApp.with(holder.art).clear(holder.art)
        }
    }

    fun setOnMusicCallbackListener(musicCallbacks: MusicCallbacks) {
        this.musicCallbacks = musicCallbacks
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val art: ImageView = itemView.findViewById(R.id.adapter_music_art)
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_name)
        val artists: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_artists)
        val album: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_album)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_music_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView)

    companion object {
        interface MusicCallbacks {
            fun onMusicClicked(uri: Uri)
        }
    }
}