
package my.poc.demo.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;

public class FrameShapeDrawable extends ShapeDrawable {

    private int mFrameColor = 0x33ffffff;
    private float mFrameWidth = 1;
    private boolean mDrawFrame;
    private Paint mPaint = new Paint();

    public FrameShapeDrawable() {
        setDither(true);
    }

    public FrameShapeDrawable(Shape s) {
        super(s);
    }

    public void setFrameWidth(int width) {
        mFrameWidth = width;
    }

    public void setFrameColor(int color) {
        mFrameColor = color;
    }

    public void setFrameEnable(boolean enable) {
        mDrawFrame = enable;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mDrawFrame) {
            mPaint.setDither(true);
            mPaint.setAntiAlias(true);
            mPaint.setColor(mFrameColor);
            mPaint.setStrokeWidth(mFrameWidth);
            mPaint.setStyle(Paint.Style.STROKE);

            Shape shape = getShape();
            Rect r = getBounds();
            if (shape instanceof OvalShape) {
                int w = r.right - r.left;
                int h = r.bottom - r.top;
                canvas.drawCircle(w / 2, h / 2, (w / 2) - mFrameWidth / 2, mPaint);
            } else if (shape instanceof RectShape) {
                canvas.drawRect(r, mPaint);
            }
        }
    }
}
