package app.simple.inure.terminal.compat;

import android.os.Build;

/**
 * The classes in this package take advantage of the fact that the VM does
 * not attempt to load a class until it's accessed, and the verifier
 * does not run until a class is loaded.  By keeping the methods which
 * are unavailable on older platforms in subclasses which are only ever
 * accessed on platforms where they are available, we can preserve
 * compatibility with older platforms without resorting to reflection.
 * <p>
 * See http://developer.android.com/resources/articles/backward-compatibility.html
 * and http://android-developers.blogspot.com/2010/07/how-to-have-your-cupcake-and-eat-it-too.html
 * for further discussion of this technique.
 */

public class AndroidCompat {
    public final static int SDK = getSDK();
    
    // The era of Holo Design
    public final static boolean V11ToV20;
    
    static {
        V11ToV20 = (SDK >= 11) && (SDK <= Build.VERSION.SDK_INT);
    }
    
    private static int getSDK() {
        return Build.VERSION.SDK_INT;
    }
}
