package my.poc.demo.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;


/**
 * 气泡背景层
 */
public class BubbleDrawable extends ColorDrawable {

    private final Paint mPaint;
    private final Path mPath;
    private final int mNormalColor;
    private final int mPressColor;
    private boolean isPressed;

    public BubbleDrawable(int normalColor, int pressColor, Path displayPath) {
        mPath = displayPath;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mNormalColor = normalColor;
        mPressColor = pressColor;
        mPaint.setColor(normalColor);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mPath != null) {
            canvas.clipPath(mPath);
        }
        canvas.drawRect(getBounds(), mPaint);
    }

    @Override
    protected boolean onStateChange(int[] state) {
        isPressed = checkPressed(state);
        mPaint.setColor(isPressed ? mPressColor : mNormalColor);
        invalidateSelf();
        return true;
    }

    @Override
    public boolean setState(int[] stateSet) {
        return super.setState(stateSet);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    private boolean checkPressed(int[] state) {
        boolean pressed = false;
        for (int i = 0, j = state != null ? state.length : 0; i < j; i++) {
            if (state[i] == android.R.attr.state_pressed) {
                pressed = true;
                break;
            }
        }
        return pressed;
    }
}
