package app.simple.inure.adapters.installer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.decorations.overscroll.VerticalListViewHolder
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.models.User

class AdapterUsers(private val users: ArrayList<User>, private val function: (User) -> Unit) : RecyclerView.Adapter<AdapterUsers.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_users, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val user = users[position]

        holder.name.text = user.name
        holder.id.text = buildString {
            append(user.id)
            append(" | ")
            append(user.hexFlags)
        }

        holder.container.setOnClickListener {
            function(user)
        }
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class Holder(itemView: View) : VerticalListViewHolder(itemView) {
        val name: TypeFaceTextView = itemView.findViewById(R.id.name)
        val id: TypeFaceTextView = itemView.findViewById(R.id.id)
        val container: View = itemView.findViewById(R.id.container)
    }
}