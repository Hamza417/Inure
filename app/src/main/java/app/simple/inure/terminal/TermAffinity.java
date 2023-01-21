package app.simple.inure.terminal;

import android.os.Bundle;
import android.util.Log;

public class TermAffinity extends Term {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TermAffinity", "Created a new unique affinity term instance");
    }
}
