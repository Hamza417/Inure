package app.simple.inure.adapters.music

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.condensed.CondensedDynamicRippleConstraintLayout
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptor
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptorWithoutTransform
import app.simple.inure.glide.util.AudioCoverUtil.loadFromUri
import app.simple.inure.glide.util.AudioCoverUtil.loadFromUriWithoutTransform
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.util.RecyclerViewUtils

class AdapterMusic(val list: ArrayList<AudioModel>, val headerMode: Boolean) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var musicCallbacks: MusicCallbacks? = null
    private var lastHighlightId = MusicPreferences.getLastMusicId()

    var id = MusicPreferences.getLastMusicId()

    private val useFelicityFlowInterface = DevelopmentPreferences.get(DevelopmentPreferences.USE_PERISTYLE_INTERFACE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        if (headerMode) {
            return when (viewType) {
                RecyclerViewUtils.TYPE_HEADER -> {
                    Header(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_header_music, parent, false))
                }
                RecyclerViewUtils.TYPE_ITEM -> {
                    if (useFelicityFlowInterface) {
                        Holder(LayoutInflater.from(parent.context)
                                   .inflate(R.layout.adapter_music_flow, parent, false))
                    } else {
                        Holder(LayoutInflater.from(parent.context)
                                   .inflate(R.layout.adapter_music, parent, false))
                    }
                }
                else -> {
                    throw IllegalArgumentException("there is no type that matches the type" +
                                                           " $viewType, make sure your using types correctly")
                }
            }
        } else {
            return when {
                useFelicityFlowInterface -> {
                    Holder(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_music_flow, parent, false))
                }
                else -> {
                    Holder(LayoutInflater.from(parent.context)
                               .inflate(R.layout.adapter_music, parent, false))
                }
            }
        }
    }

    override fun onBindViewHolder(holder: VerticalListViewHolder, holderPosition: Int) {
        val position = if (headerMode) holderPosition - 1 else holderPosition
        val minusValue = if (headerMode) 1 else 0

        if (holder is Holder) {
            holder.title.text = list[position].title
            holder.artists.text = list[position].artists
            holder.album.text = list[position].album

            holder.art.transitionName = list[position].fileUri

            if (useFelicityFlowInterface) {
                if (DevelopmentPreferences.get(DevelopmentPreferences.LOAD_ALBUM_ART_FROM_FILE)) {
                    holder.art.loadFromFileDescriptorWithoutTransform(list[position].fileUri.toUri())
                } else {
                    holder.art.loadFromUriWithoutTransform(Uri.parse(list[position].artUri))
                }
            } else {
                if (DevelopmentPreferences.get(DevelopmentPreferences.LOAD_ALBUM_ART_FROM_FILE)) {
                    holder.art.loadFromFileDescriptor(list[position].fileUri.toUri())
                } else {
                    holder.art.loadFromUri(Uri.parse(list[position].artUri))
                }
            }

            holder.container.setDefaultBackground(MusicPreferences.getLastMusicId() == list[position].id)

            holder.container.setOnClickListener {
                id = list[holder.bindingAdapterPosition.minus(minusValue)].id
                MusicPreferences.setMusicPosition(holder.bindingAdapterPosition.minus(minusValue))
                musicCallbacks?.onMusicClicked(list[holder.bindingAdapterPosition.minus(minusValue)],
                                               holder.art,
                                               holder.bindingAdapterPosition.minus(minusValue))
                // We need the animations, this will break it
                // updateHighlightedSongState()
            }

            holder.container.setOnLongClickListener {
                musicCallbacks?.onMusicLongClicked(list[holder.bindingAdapterPosition.minus(minusValue)],
                                                   holder.art, holder.bindingAdapterPosition.minus(minusValue), it)
                true
            }

            if (useFelicityFlowInterface) {
                holder.container.removeRipple()
            }
        } else if (holder is Header) {
            holder.total.text = buildString {
                list.size.toString()
            }
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
        id = MusicPreferences.getLastMusicId()

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

    fun updateDeleted(position: Int) {
        if (headerMode) {
            list.removeAt(position)
            notifyItemChanged(0)
            notifyItemRemoved(position.plus(1))
            notifyItemRangeChanged(0, list.size)
        } else {
            list.removeAt(position)
            notifyItemChanged(0)
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, list.size)
        }
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val art: ImageView = itemView.findViewById(R.id.adapter_music_art)
        val title: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_name)
        val artists: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_artists)
        val album: TypeFaceTextView = itemView.findViewById(R.id.adapter_music_album)
        val container: CondensedDynamicRippleConstraintLayout = itemView.findViewById(R.id.adapter_music_container)
    }

    inner class Header(itemView: View) : VerticalListViewHolder(itemView) {
        val total: TypeFaceTextView = itemView.findViewById(R.id.total)
    }

    companion object {
        interface MusicCallbacks {
            fun onMusicClicked(audioModel: AudioModel, art: ImageView, position: Int)
            fun onMusicLongClicked(audioModel: AudioModel, view: ImageView, position: Int, container: View)
        }
    }
}
