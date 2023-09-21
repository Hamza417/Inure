package app.simple.inure.glide.uricover

import android.content.Context
import android.net.Uri

class UriCoverModel(val context: Context, val artUri: Uri) {
    override fun hashCode(): Int {
        return artUri.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is UriCoverModel) {
            return artUri == other.artUri
        }
        return false
    }

    override fun toString(): String {
        return "UriCoverModel(context=$context, artUri=$artUri)"
    }
}