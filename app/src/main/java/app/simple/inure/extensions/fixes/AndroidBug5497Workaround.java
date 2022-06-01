package app.simple.inure.extensions.fixes;

import android.graphics.Rect;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AndroidBug5497Workaround {
    
    // For more information, see https://issuetracker.google.com/issues/36911528
    // To use this class, simply invoke assistActivity() on an Activity that already has its content view set.
    
    public static void assistActivity(AppCompatActivity activity) {
        new AndroidBug5497Workaround(activity);
    }
    
    private final View mChildOfContent;
    private int usableHeightPrevious;
    private final FrameLayout.LayoutParams frameLayoutParams;
    
    private AndroidBug5497Workaround(AppCompatActivity activity) {
        FrameLayout content = (FrameLayout) activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(this :: possiblyResizeChildOfContent);
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }
    
    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard / 4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            }
            else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }
    
    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom - r.top;
    }
}
