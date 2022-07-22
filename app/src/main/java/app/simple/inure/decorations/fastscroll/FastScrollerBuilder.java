/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.simple.inure.decorations.fastscroll;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Consumer;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.RecyclerView;
import app.simple.inure.R;
import app.simple.inure.preferences.AppearancePreferences;

public class FastScrollerBuilder {
    
    @NonNull
    private final ViewGroup mView;
    
    @Nullable
    private FastScroller.ViewHelper mViewHelper;
    
    @Nullable
    private PopupTextProvider mPopupTextProvider;
    
    @Nullable
    private Rect mPadding;
    
    private Drawable mTrackDrawable;
    private Drawable mThumbDrawable;
    private Consumer <TextView> mPopupStyle;
    
    @Nullable
    private FastScroller.AnimationHelper mAnimationHelper;
    
    public FastScrollerBuilder(@NonNull ViewGroup view) {
        mView = view;
        setupAesthetics();
    }
    
    @NonNull
    public FastScrollerBuilder setViewHelper(@Nullable FastScroller.ViewHelper viewHelper) {
        mViewHelper = viewHelper;
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setPopupTextProvider(@Nullable PopupTextProvider popupTextProvider) {
        mPopupTextProvider = popupTextProvider;
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setPadding(int left, int top, int right, int bottom) {
        if (mPadding == null) {
            mPadding = new Rect();
        }
        mPadding.set(left, top, right, bottom);
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setPadding(@Nullable Rect padding) {
        if (padding != null) {
            if (mPadding == null) {
                mPadding = new Rect();
            }
            mPadding.set(padding);
        }
        else {
            mPadding = null;
        }
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setTrackDrawable(@NonNull Drawable trackDrawable) {
        mTrackDrawable = trackDrawable;
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setThumbDrawable(@NonNull Drawable thumbDrawable) {
        mThumbDrawable = thumbDrawable;
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setPopupStyle(@NonNull Consumer <TextView> popupStyle) {
        mPopupStyle = popupStyle;
        return this;
    }
    
    @NonNull
    public FastScrollerBuilder setupAesthetics() {
        Context context = mView.getContext();
        mTrackDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.fast_scroller_track, context.getTheme());
        mThumbDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.fast_scroller_thumb, context.getTheme());
        mThumbDrawable.setTintList(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
        mPopupStyle = PopupStyles.Inure;
        return this;
    }
    
    public void setAnimationHelper(@Nullable FastScroller.AnimationHelper animationHelper) {
        mAnimationHelper = animationHelper;
    }
    
    public void disableScrollbarAutoHide() {
        DefaultAnimationHelper animationHelper = new DefaultAnimationHelper(mView);
        animationHelper.setScrollbarAutoHideEnabled(false);
        mAnimationHelper = animationHelper;
    }
    
    @NonNull
    public FastScroller build() {
        return new FastScroller(mView, getOrCreateViewHelper(), mPadding, mTrackDrawable,
                mThumbDrawable, mPopupStyle, getOrCreateAnimationHelper());
    }
    
    @NonNull
    private FastScroller.ViewHelper getOrCreateViewHelper() {
        if (mViewHelper != null) {
            return mViewHelper;
        }
        if (mView instanceof ViewHelperProvider) {
            return ((ViewHelperProvider) mView).getViewHelper();
        }
        else if (mView instanceof RecyclerView) {
            return new RecyclerViewHelper((RecyclerView) mView, mPopupTextProvider);
        }
        else if (mView instanceof NestedScrollView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollNestedScrollView.class.getSimpleName() + " instead of "
                    + NestedScrollView.class.getSimpleName() + "for fast scroll");
        }
        else if (mView instanceof ScrollView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollScrollView.class.getSimpleName() + " instead of "
                    + ScrollView.class.getSimpleName() + "for fast scroll");
        } else if (mView instanceof WebView) {
            throw new UnsupportedOperationException("Please use "
                    + FastScrollWebView.class.getSimpleName() + " instead of "
                    + WebView.class.getSimpleName() + "for fast scroll");
        } else {
            throw new UnsupportedOperationException(mView.getClass().getSimpleName()
                    + " is not supported for fast scroll");
        }
    }
    
    public void updateAesthetics() {
        mThumbDrawable.setTintList(ColorStateList.valueOf(AppearancePreferences.INSTANCE.getAccentColor()));
    }
    
    @NonNull
    private FastScroller.AnimationHelper getOrCreateAnimationHelper() {
        if (mAnimationHelper != null) {
            return mAnimationHelper;
        }
        return new DefaultAnimationHelper(mView);
    }
}
