package app.simple.inure.terminal;

import android.os.Bundle;
import android.util.Log;

public class TermAffinity extends Term {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Log.d("TermAffinity", "Created a new unique affinity term instance");
    }
}
