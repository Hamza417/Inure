package com.kekstudio.dachshundtablayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kekstudio.dachshundtablayout.indicators.AnimatedIndicatorInterface;
import com.kekstudio.dachshundtablayout.indicators.AnimatedIndicatorType;
import com.kekstudio.dachshundtablayout.indicators.DachshundIndicator;
import com.kekstudio.dachshundtablayout.indicators.LineFadeIndicator;
import com.kekstudio.dachshundtablayout.indicators.LineMoveIndicator;
import com.kekstudio.dachshundtablayout.indicators.PointFadeIndicator;
import com.kekstudio.dachshundtablayout.indicators.PointMoveIndicator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Created by Andy671
 * <p>
 * Added ViewPager2 support by Hamza417 (7 Sept, 2022)
 */

public class DachshundTabLayout extends TabLayout implements ViewPager.OnPageChangeListener {
    
    private static final int DEFAULT_HEIGHT_DP = 6;
    
    private int mIndicatorColor;
    private int mIndicatorHeight;
    
    private int mCurrentPosition;
    
    private boolean mCenterAlign;
    
    private LinearLayout mTabStrip;
    
    @Nullable
    private TabLayoutMediator tabLayoutMediator;
    private AnimatedIndicatorType mAnimatedIndicatorType;
    private AnimatedIndicatorInterface mAnimatedIndicator;
    
    private int mTempPosition, mTempPositionOffsetPixels;
    private float mTempPositionOffset;
    
    public DachshundTabLayout(Context context) {
        this(context, null);
    }
    
    public DachshundTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public DachshundTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        super.setSelectedTabIndicatorHeight(0);
        
        mTabStrip = (LinearLayout) super.getChildAt(0);
        
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DachshundTabLayout);
        
        this.mIndicatorHeight = ta.getDimensionPixelSize(R.styleable.DachshundTabLayout_ddIndicatorHeight, HelperUtils.dpToPx(DEFAULT_HEIGHT_DP));
        this.mIndicatorColor = ta.getColor(R.styleable.DachshundTabLayout_ddIndicatorColor, Color.WHITE);
        this.mCenterAlign = ta.getBoolean(R.styleable.DachshundTabLayout_ddCenterAlign, false);
        this.mAnimatedIndicatorType = AnimatedIndicatorType.values()[ta.getInt(R.styleable.DachshundTabLayout_ddAnimatedIndicator, 0)];
        
        ta.recycle();
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        
        if (mCenterAlign) {
            View firstTab = ((ViewGroup) getChildAt(0)).getChildAt(0);
            View lastTab = ((ViewGroup) getChildAt(0)).getChildAt(((ViewGroup) getChildAt(0)).getChildCount() - 1);
            ViewCompat.setPaddingRelative(getChildAt(0),
                    (getWidth() / 2) - (firstTab.getWidth() / 2),
                    0,
                    (getWidth() / 2) - (lastTab.getWidth() / 2),
                    0);
        }
        
        if (mAnimatedIndicator == null) {
            setupAnimatedIndicator();
        }
        
        onPageScrolled(mTempPosition, mTempPositionOffset, mTempPositionOffsetPixels);
    }
    
    private void setupAnimatedIndicator() {
        switch (mAnimatedIndicatorType) {
            case DACHSHUND:
                setAnimatedIndicator(new DachshundIndicator(this));
                break;
            case POINT_MOVE:
                setAnimatedIndicator(new PointMoveIndicator(this));
                break;
            case LINE_MOVE:
                setAnimatedIndicator(new LineMoveIndicator(this));
                break;
            case POINT_FADE:
                setAnimatedIndicator(new PointFadeIndicator(this));
                break;
            case LINE_FADE:
                setAnimatedIndicator(new LineFadeIndicator(this));
                break;
        }
    }
    
    public void setAnimatedIndicator(AnimatedIndicatorInterface animatedIndicator) {
        this.mAnimatedIndicator = animatedIndicator;
        
        animatedIndicator.setSelectedTabIndicatorColor(mIndicatorColor);
        animatedIndicator.setSelectedTabIndicatorHeight(mIndicatorHeight);
        
        invalidate();
    }
    
    @Override
    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        this.mIndicatorColor = color;
        if (mAnimatedIndicator != null) {
            mAnimatedIndicator.setSelectedTabIndicatorColor(color);
            
            invalidate();
        }
    }
    
    @Override
    public void setSelectedTabIndicatorHeight(int height) {
        this.mIndicatorHeight = height;
        if (mAnimatedIndicator != null) {
            mAnimatedIndicator.setSelectedTabIndicatorHeight(height);
            
            invalidate();
        }
        
    }
    
    public void setCenterAlign(boolean centerAlign) {
        this.mCenterAlign = centerAlign;
        
        requestLayout();
    }
    
    @Override
    public void setupWithViewPager(@Nullable ViewPager viewPager) {
        this.setupWithViewPager(viewPager, true);
    }
    
    @Override
    public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
        super.setupWithViewPager(viewPager, autoRefresh);
        
        //TODO
        if (viewPager != null) {
            viewPager.removeOnPageChangeListener(this);
            viewPager.addOnPageChangeListener(this);
        }
    }
    
    /**
     * Method to user ViewPager2 with the tab layout
     * <p>
     * Make sure to attach the adapter to ViewPager2 first before
     * calling this method.
     *
     * @param viewPager2 instance of the ViewPager2
     * @param titles     String array of title to be shown on the map
     */
    public void setupWithViewPager2(@Nullable ViewPager2 viewPager2, String[] titles) {
        if (viewPager2 != null) {
            viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                    DachshundTabLayout.this.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            });
            
            tabLayoutMediator = new TabLayoutMediator(this, viewPager2, (tab, position) -> {
                tab.setText(titles[position]);
                viewPager2.setCurrentItem(position, true);
            });
            
            tabLayoutMediator.attach();
        }
    }
    
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        if (mAnimatedIndicator != null) {
            mAnimatedIndicator.draw(canvas);
        }
    }
    
    @Override
    public void onPageScrollStateChanged(final int state) {
    }
    
    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        this.mTempPosition = position;
        this.mTempPositionOffset = positionOffset;
        this.mTempPositionOffsetPixels = positionOffsetPixels;
        
        if ((position > mCurrentPosition) || (position + 1 < mCurrentPosition)) {
            mCurrentPosition = position;
        }
        
        int mStartXLeft, mStartXCenter, mStartXRight, mEndXLeft, mEndXCenter, mEndXRight;
        
        if (position != mCurrentPosition) {
            
            mStartXLeft = (int) getChildXLeft(mCurrentPosition);
            mStartXCenter = (int) getChildXCenter(mCurrentPosition);
            mStartXRight = (int) getChildXRight(mCurrentPosition);
            
            mEndXLeft = (int) getChildXLeft(position);
            mEndXRight = (int) getChildXRight(position);
            mEndXCenter = (int) getChildXCenter(position);
            
            if (mAnimatedIndicator != null) {
                mAnimatedIndicator.setIntValues(mStartXLeft, mEndXLeft,
                        mStartXCenter, mEndXCenter,
                        mStartXRight, mEndXRight);
                mAnimatedIndicator.setCurrentPlayTime((long) ((1 - positionOffset) * (int) mAnimatedIndicator.getDuration()));
            }
            
        } else {
            
            mStartXLeft = (int) getChildXLeft(mCurrentPosition);
            mStartXCenter = (int) getChildXCenter(mCurrentPosition);
            mStartXRight = (int) getChildXRight(mCurrentPosition);
            
            if (mTabStrip.getChildAt(position + 1) != null) {
                
                mEndXLeft = (int) getChildXLeft(position + 1);
                mEndXCenter = (int) getChildXCenter(position + 1);
                mEndXRight = (int) getChildXRight(position + 1);
                
            } else {
                mEndXLeft = (int) getChildXLeft(position);
                mEndXCenter = (int) getChildXCenter(position);
                mEndXRight = (int) getChildXRight(position);
            }
            
            if (mAnimatedIndicator != null) {
                mAnimatedIndicator.setIntValues(mStartXLeft, mEndXLeft,
                        mStartXCenter, mEndXCenter,
                        mStartXRight, mEndXRight);
                mAnimatedIndicator.setCurrentPlayTime((long) (positionOffset * (int) mAnimatedIndicator.getDuration()));
            }
            
        }
        
        if (positionOffset == 0) {
            mCurrentPosition = position;
        }
    }
    
    @Override
    public void onPageSelected(final int position) {
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (tabLayoutMediator != null) {
            if (tabLayoutMediator.isAttached()) {
                tabLayoutMediator.detach();
            }
        }
    }
    
    public int getCurrentPosition() {
        return mCurrentPosition;
    }
    
    public float getChildXLeft(int position) {
        View tab = mTabStrip.getChildAt(position);
        if (tab != null) {
            return tab.getX();
        } else {
            return 0;
        }
    }
    
    public float getChildXCenter(int position) {
        View tab = mTabStrip.getChildAt(position);
        if (tab != null) {
            int width = tab.getWidth() == 0 ? tab.getMeasuredWidth() : tab.getWidth();
            return tab.getX() + width / 2F;
        } else {
            return 0;
        }
    }
    
    public float getChildXRight(int position) {
        View tab = mTabStrip.getChildAt(position);
        if (tab != null) {
            int width = tab.getWidth() == 0 ? tab.getMeasuredWidth() : tab.getWidth();
            return tab.getX() + width;
        } else {
            return 0;
        }
    }
    
    public AnimatedIndicatorInterface getAnimatedIndicator() {
        return mAnimatedIndicator;
    }
}