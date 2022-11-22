package app.simple.inure.adapters.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleConstraintLayout
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.AudioCoverUtil.loadFromUri
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.util.RecyclerViewUtils

class AdapterMusic(val list: ArrayList<AudioModel>, val headerMode: Boolean) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var musicCallbacks: MusicCallbacks? = null
    private var lastHighlightId = MusicPreferences.getLastMusicId()
    private var id = MusicPreferences.getLastMusicId()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        if (headerMode) {
            return when (viewType) {
                RecyclerViewUtils.TYPE_HEADER -> {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_music, parent, false))
                }
                RecyclerViewUtils.TYPE_ITEM -> {
                    Holder(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_music, parent, false))
                }
                else -> {
                    throw IllegalArgumentException("there is no type that matches the type $viewType, make sure your using types correctly")
                }
            }
        } else {
            return Holder(LayoutInflater.from(parent.context)
                              .inflate(R.layout.adapter_music, parent, false))
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, position_: Int) {
        val position = if (headerMode) position_ - 1 else position_

        if (holder is Holder) {
            holder.title.text = list[position].title
            holder.artists.text = list[position].artists
            holder.album.text = list[position].album

            holder.art.loadFromUri(holder.context, Uri.parse(list[position].artUri))
            holder.container.setDefaultBackground(MusicPreferences.getLastMusicId() == list[position].id)

            holder.container.setOnClickListener {
                MusicPreferences.setLastMusicId(list[position].id)
                id = list[position].id
                musicCallbacks?.onMusicClicked(Uri.parse(list[position].fileUri))
                updateHighlightedSongState()
            }
        } else if (holder is Header) {
            holder.total.text = list.size.toString()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (headerMode) {
            if (position == 0) {
                RecyclerViewUtils.TYPE_HEADER
            } else RecyclerViewUtils.TYPE_ITEM
        } else {
            RecyclerViewUtils.TYPE_ITEM
        }
    }

    override fun getItemCount(): Int {
        return list.size.plus(if (headerMode) 1 else 0)
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

    fun updateHighlightedSongState() {
        val compensate = if (headerMode) 1 else 0
        for (i in list.indices) {
            if (list[i].id == id) {
                notifyItemChanged(i.plus(compensate))
            }
            if (list[i].id == lastHighlightId) {
                notifyItemChanged(i.plus(compensate))
            }
        }

        lastHighlightId = id
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val art: ImageView = itemView.findViewById(R.id.adapter_music_art)
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_name)
        val artists: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_artists)
        val album: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_album)
        val container: DynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_music_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total)
    }

    companion object {
        interface MusicCallbacks {
            fun onMusicClicked(uri: Uri)
            fun onMusicSearchClicked()
            fun onMusicPlayClicked(position: Int)
        }
    }
}