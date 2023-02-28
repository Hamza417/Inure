package app.simple.inure.glide.svg;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

public class SVG {
    private final Uri uri;
    private final Context context;
    
    public SVG(@NonNull Context context, @NonNull Uri uri) {
        this.context = context;
        this.uri = uri;
    }
    
    public Uri getUri() {
        return uri;
    }
    
    public Context getContext() {
        return context;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "SVG{" + "uri=" + uri + '}';
    }
}
