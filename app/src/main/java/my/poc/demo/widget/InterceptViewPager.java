
package my.poc.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class InterceptViewPager extends ViewPager {

    private boolean canTouchScrollable;

    public InterceptViewPager(Context context) {
        super(context);
    }

    public InterceptViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        try {
            if (canTouchScrollable) {
                return super.onTouchEvent(arg0);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        try {
            if (canTouchScrollable) {
                return super.onInterceptTouchEvent(arg0);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setTouchScrollable(boolean enable) {
        canTouchScrollable = enable;
    }

    public boolean canTouchScrollable() {
        return canTouchScrollable;
    }

}
