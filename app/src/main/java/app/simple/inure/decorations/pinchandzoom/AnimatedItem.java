package app.simple.inure.decorations.pinchandzoom;

import android.graphics.Rect;

import androidx.recyclerview.widget.RecyclerView;

public class AnimatedItem {
    public enum Type {
        DISAPPEARANCE, APPEARANCE, PERSISTENCE, CHANGE
    }
    
    private final RecyclerView.ViewHolder viewHolder;
    
    private final Rect preRect;
    private final Rect postRect;
    
    private final Type type;
    
    public static class Builder {
        private RecyclerView.ViewHolder viewHolder;
        
        private Rect preRect;
        private Rect postRect;
        
        private Type type;
        
        public Builder setViewHolder(RecyclerView.ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
            return this;
        }
        
        public Builder setPreRect(RecyclerView.ItemAnimator.ItemHolderInfo preLayoutInfo) {
            if (preLayoutInfo == null) {
                preRect = new Rect();
            } else {
                this.preRect = new Rect(preLayoutInfo.left, preLayoutInfo.top, preLayoutInfo.right, preLayoutInfo.bottom);
            }
            return this;
        }
        
        public Builder setPostRect(RecyclerView.ItemAnimator.ItemHolderInfo postLayoutInfo) {
            if (postLayoutInfo == null) {
                postRect = new Rect();
            } else {
                this.postRect = new Rect(postLayoutInfo.left, postLayoutInfo.top, postLayoutInfo.right, postLayoutInfo.bottom);
            }
            return this;
        }
        
        public Builder setType(Type type) {
            this.type = type;
            return this;
        }
        
        public AnimatedItem build() {
            return new AnimatedItem(viewHolder, preRect, postRect, type);
        }
    }
    
    public AnimatedItem(RecyclerView.ViewHolder viewHolder, Rect preRect, Rect postRect, Type type) {
        this.viewHolder = viewHolder;
        this.preRect = preRect;
        this.postRect = postRect;
        this.type = type;
    }
    
    public RecyclerView.ViewHolder getViewHolder() {
        return viewHolder;
    }
    
    public Rect getPreRect() {
        return preRect;
    }
    
    public Rect getPostRect() {
        return postRect;
    }
    
    public Type getType() {
        return type;
    }
}
