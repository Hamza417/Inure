package app.simple.inure.decorations.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks;

public class BaseAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {
    
    private AppsAdapterCallbacks appsAdapterCallbacks;
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    
    }
    
    @Override
    public int getItemCount() {
        return 0;
    }
    
    public void setOnItemClickListener(AppsAdapterCallbacks appsAdapterCallbacks) {
        this.appsAdapterCallbacks = appsAdapterCallbacks;
    }
}
