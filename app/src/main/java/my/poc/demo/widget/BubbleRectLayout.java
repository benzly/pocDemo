package my.poc.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * 裁剪一个气泡显示区域, 默认关闭此功能
 */
public class BubbleRectLayout extends RelativeLayout {

    private RectF mRoundRect;
    private final Path mRoundPath = new Path();
    private final Path mArrowPath = new Path();
    private final Paint mCoverLayerPaint = new Paint();
    private final Paint mFramePaint = new Paint();
    private ChatMessageDirection mChatDirection = ChatMessageDirection.RIGHT;
    private boolean mBubbleEnable;
    private boolean mArrowEnable;
    private boolean mShdowEnable;
    private boolean mFrameEnable;
    private int mLeftRadius;
    private int mTopRadius;
    private int mRightRadius;
    private int mBottomRadius;

    private BubbleDrawable mStateChangedDrawable = new BubbleDrawable(0, 0, null);

    public BubbleRectLayout(Context context) {
        this(context, null);
    }

    public BubbleRectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void setChatDirection(ChatMessageDirection direction) {
        mChatDirection = direction;
    }

    protected void setBubbleEnable(boolean enable) {
        mBubbleEnable = enable;
    }

    protected void setArrowEnable(boolean enable) {
        mArrowEnable = enable;
    }

    protected void setShadowEnable(boolean enable) {
        mShdowEnable = enable;
    }

    protected void setRadius(int radius) {
        setRadius(radius, radius, radius, radius);
    }

    protected void setRadius(int left, int top, int right, int bottom) {
        mLeftRadius = left;
        mTopRadius = top;
        mRightRadius = right;
        mBottomRadius = bottom;
    }

    protected void setBubbleBackground(int color) {
        setBackground(new BubbleDrawable(color, color, mRoundPath));
    }

    protected void setFrameEnable(boolean enable) {
        mFrameEnable = enable;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBubbleEnable) {
            mArrowPath.reset();
            float mArrowWidth = mArrowEnable ? 10 : 0;
            /**
             * 1、画一个三角形区域
             * 2、画一个圆角区域
             * 3、取它们的合集
             */
            float mArrowPointStartY = 30;
            float mArrowPointCenterY = 30 + mArrowWidth;
            float mArrowPointEndY = 30 + 2 * mArrowWidth;
            if (mChatDirection == ChatMessageDirection.LEFT) {
                mRoundRect = new RectF(mArrowWidth, 0, this.getWidth(), this.getHeight());
                mArrowPath.moveTo(0, mArrowPointCenterY);
                mArrowPath.lineTo(mArrowWidth, mArrowPointStartY);
                mArrowPath.lineTo(mArrowWidth, mArrowPointEndY);
                mArrowPath.lineTo(0, mArrowPointCenterY);
            } else {
                mRoundRect = new RectF(0, 0, this.getWidth() - mArrowWidth, this.getHeight());
                mArrowPath.moveTo(this.getWidth() - mArrowWidth, mArrowPointStartY);
                mArrowPath.lineTo(this.getWidth(), mArrowPointCenterY);
                mArrowPath.lineTo(this.getWidth() - mArrowWidth, mArrowPointEndY);
            }
            mArrowPath.close();

            float[] radius = new float[]{mLeftRadius, mLeftRadius, mTopRadius, mTopRadius,
                    mRightRadius, mRightRadius, mBottomRadius, mBottomRadius};
            mRoundPath.addRoundRect(mRoundRect, radius, Path.Direction.CW);

            if (mArrowEnable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mRoundPath.op(mArrowPath, Path.Op.UNION);
            }

            mFramePaint.setStyle(Paint.Style.STROKE);
            mFramePaint.setStrokeWidth(2);
            mFramePaint.setAntiAlias(true);
            mFramePaint.setColor(Color.parseColor("#20000000"));

            mCoverLayerPaint.setColor(Color.parseColor("#60FFFFFF"));
            mCoverLayerPaint.setAntiAlias(true);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        //裁剪气泡
        if (mBubbleEnable && mRoundRect != null) {
            canvas.clipPath(mRoundPath);
        }
        super.dispatchDraw(canvas);
        if (mFrameEnable) {
            canvas.drawPath(mRoundPath, mFramePaint);
        }

        // 绘制蒙层，仅支持有background的情况，因为当前view不建议拦截按键，只会从父view获取状态
        // 当有background时，能得到父view的按压状态
        if (mBubbleEnable && mShdowEnable) {
            Drawable bg = getBackground();
            if (bg != null && isPressedState(bg.getState())) {
                canvas.drawPath(mRoundPath, mCoverLayerPaint);
            }
        }
    }

    private boolean isPressedState(int[] state) {
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
