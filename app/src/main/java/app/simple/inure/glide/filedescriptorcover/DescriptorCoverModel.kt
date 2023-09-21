package app.simple.inure.glide.filedescriptorcover

import android.content.Context
import android.net.Uri

class DescriptorCoverModel(val context: Context, val fileUri: Uri) {

    override fun hashCode(): Int {
        return fileUri.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is DescriptorCoverModel) {
            return fileUri == other.fileUri
        }
        return false
    }

    override fun toString(): String {
        return "DescriptorCoverModel(context=$context, fileUri=$fileUri)"
    }
}