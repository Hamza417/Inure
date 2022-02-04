package app.simple.inure.adapters.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.emulatorview.TermSession
import app.simple.inure.decorations.emulatorview.UpdateCallback
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.terminal.GenericTermSession
import app.simple.inure.terminal.util.SessionList
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull

open class AdapterWindows(private var sessions: SessionList) : RecyclerView.Adapter<AdapterWindows.Holder>(), UpdateCallback {

    private var adapterWindowsCallback: AdapterWindowsCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_windows, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val defaultTitle: String = holder.itemView.context.getString(R.string.window_title, position + 1)
        holder.label.text = getSessionTitle(position, defaultTitle)

        holder.label.setOnClickListener {
            adapterWindowsCallback?.onWindowClicked(position)
        }

        holder.close.setOnClickListener {
            adapterWindowsCallback?.onClose(position)
        }
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    override fun onUpdate(position: Int) {
        notifyItemRemoved(position)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onUpdate() {
        notifyDataSetChanged()
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val label: DynamicRippleTextView = itemView.findViewById(R.id.window_list_label)
        val close: DynamicRippleImageButton = itemView.findViewById(R.id.window_list_close)
    }

    fun setOnAdapterWindowsCallbackListener(adapterWindowsCallback: AdapterWindowsCallback) {
        this.adapterWindowsCallback = adapterWindowsCallback
    }

    fun setSessions(sessions: SessionList?) {
        if (sessions.isNull()) {
            onUpdate()
            return
        }

        this.sessions = sessions!!
        sessions.addCallback(this)
        sessions.addTitleChangedListener(this)
    }

    fun getSessionTitle(position: Int, defaultTitle: String?): String? {
        val session: TermSession = sessions[position]
        return if (session is GenericTermSession) {
            session.getTitle(defaultTitle)
        } else {
            defaultTitle
        }
    }

    fun getSessionTitle(position: Int, context: Context): String? {
        val defaultTitle: String = context.getString(R.string.window_title, position + 1)
        val session: TermSession = sessions[position]
        return if (session is GenericTermSession) {
            session.getTitle(defaultTitle)
        } else {
            defaultTitle
        }
    }

    open fun getCount(): Int {
        return if (sessions.isNotNull()) {
            sessions.size
        } else {
            0
        }
    }

    open fun getItem(position: Int): TermSession? {
        return sessions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    companion object {
        interface AdapterWindowsCallback {
            fun onWindowClicked(position: Int)
            fun onClose(position: Int)
        }
    }
}