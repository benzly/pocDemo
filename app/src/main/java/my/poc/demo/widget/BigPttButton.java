package my.poc.demo.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import my.poc.demo.R;

/**
 * Created by benz on 2016/10/19.
 */
public class BigPttButton extends RelativeLayout {

    public interface OnPressAction {
        /**
         * 完全按下
         */
        void onPressDownFull();

        /**
         * 松开
         */
        void onPressUp();
    }

    class CircleFrameLayer extends View {
        Paint bgPaint = new Paint();
        Paint dottedLinePaint = new Paint();
        boolean showRotatedAni;
        int disableColor = Color.BLACK;
        int normalColor = Color.parseColor("#009688");
        int talkingColor = Color.parseColor("#84d045");
        int remoteTalkingColor = Color.parseColor("#ff0202");
        int requestingColor = Color.parseColor("#FEBD25");
        int dottedLineWidth;

        public CircleFrameLayer(Context context) {
            super(context);

            dottedLinePaint.setAntiAlias(true);
            dottedLinePaint.setStyle(Paint.Style.STROKE);
            dottedLinePaint.setColor(talkingColor);

            bgPaint.setAntiAlias(true);
            bgPaint.setColor(normalColor);
            bgPaint.setStyle(Paint.Style.STROKE);

            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    dottedLineWidth = getWidth() * 31 / 886;
                    DashPathEffect effects = new DashPathEffect(new float[]{dottedLineWidth, dottedLineWidth * 2}, 1);
                    dottedLinePaint.setPathEffect(effects);
                    dottedLinePaint.setStrokeWidth(dottedLineWidth);
                    bgPaint.setStrokeWidth(dottedLineWidth);
                    return false;
                }
            });
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int r = getWidth();
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, (r - dottedLineWidth * 3) / 2, bgPaint);

            if (showRotatedAni) {
                canvas.drawCircle(getWidth() / 2, getHeight() / 2, (r - dottedLineWidth * 3) / 2, dottedLinePaint);
            }
        }

        public void onStateChanged(int state) {
            showRotatedAni = false;
            Animation ani = getAnimation();
            if (ani != null) {
                ani.cancel();
            }
            clearAnimation();

            switch (state) {
                case STATE_DISABLE:
                case STATE_ERROR:
                    bgPaint.setColor(disableColor);
                    break;
                case STATE_IDLE:
                    bgPaint.setColor(normalColor);
                    break;
                case STATE_REQUEST:
                    bgPaint.setColor(requestingColor);
                    showRotatedAni = true;
                    doRotateAni();
                    break;
                case STATE_I_TALKING:
                    bgPaint.setColor(talkingColor);
                    break;
                case STATE_R_TALKING:
                    bgPaint.setColor(remoteTalkingColor);
                    break;
            }
            invalidate();
        }

        private void doRotateAni() {
            Animation ani = getAnimation();
            if (ani == null) {
                ani = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                ani.setInterpolator(new LinearInterpolator());
                ani.setDuration(10000);
                ani.setRepeatCount(-1);
            }
            startAnimation(ani);
        }
    }

    /**
     * 按钮的各个状态
     */
    public static final int STATE_IDLE = 0;
    public static final int STATE_REQUEST = 1;
    public static final int STATE_I_TALKING = 2;
    public static final int STATE_R_TALKING = 3;
    public static final int STATE_DISABLE = 4;
    public static final int STATE_ERROR = 5;

    private float mCurrentScaleX;
    private float mCurrentScaleY;
    private float mCurrentTranslationZ;
    private ScaleXUpdateListener mScaleXUpdateListener;
    private ScaleYUpdateListener mScaleYUpdateListener;
    private TranslationZUpdateListener mTranslationZUpdateListener;
    private ArrayList<Animator> mAnimators = new ArrayList<Animator>(3);

    private float mInitialValue = 0.98f;
    private FrameShapeDrawable mCircleBackground;
    private CircleFrameLayer mFloatCircleFrameLayer;
    private Bitmap mCenterIconBitmap;
    private Paint mIconPaint;
    private int mIconX;
    private int mIconY;

    private OnPressAction mOnPressAction;

    public BigPttButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClickable(true);
        setFocusable(true);

        mScaleXUpdateListener = new ScaleXUpdateListener();
        mScaleYUpdateListener = new ScaleYUpdateListener();
        mTranslationZUpdateListener = new TranslationZUpdateListener();
        mCircleBackground = new FrameShapeDrawable();

        setScaleX(mInitialValue);
        setScaleY(mInitialValue);
        mCurrentScaleX = mCurrentScaleY = mInitialValue;

        mCircleBackground.setShape(new OvalShape());
        mCircleBackground.getPaint().setColor(Color.parseColor("#212421"));
        setBackgroundDrawable(mCircleBackground);

        mFloatCircleFrameLayer = new CircleFrameLayer(context);
        addView(mFloatCircleFrameLayer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mIconPaint = new Paint();
        mIconPaint.setAntiAlias(true);
        mCenterIconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.big_talk_bt_icon);

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                mCurrentTranslationZ = 50;
                doFloatUpAnimation();
                mIconX = getWidth() / 2 - mCenterIconBitmap.getWidth() / 2;
                mIconY = getHeight() / 2 - mCenterIconBitmap.getHeight() / 2;
                return false;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(mCenterIconBitmap, mIconX, mIconY, mIconPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            doFloatDownAnimation();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            doFloatUpAnimation();
        }
        return super.onTouchEvent(event);
    }

    public void setOnPressAction(OnPressAction l) {
        mOnPressAction = l;
    }

    public void onStateChanged(int state) {
        mFloatCircleFrameLayer.onStateChanged(state);
    }

    private void doFloatUpAnimation() {
        for (Animator a : mAnimators) {
            a.cancel();
        }
        mAnimators.clear();

        ObjectAnimator xScaleAnim = ObjectAnimator.ofFloat(this, "scaleX", mCurrentScaleX, 1.0f);
        ObjectAnimator yScaleAnim = ObjectAnimator.ofFloat(this, "scaleY", mCurrentScaleY, 1.0f);
        xScaleAnim.setDuration(100);
        yScaleAnim.setDuration(100);
        xScaleAnim.addUpdateListener(mScaleXUpdateListener);
        yScaleAnim.addUpdateListener(mScaleYUpdateListener);

        ObjectAnimator upAnim = ObjectAnimator.ofFloat(this, "translationZ", mCurrentTranslationZ, 50);
        upAnim.setInterpolator(new DecelerateInterpolator());
        upAnim.setDuration(80);
        upAnim.addUpdateListener(mTranslationZUpdateListener);

        mAnimators.add(xScaleAnim);
        mAnimators.add(yScaleAnim);
        mAnimators.add(upAnim);
        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(mAnimators);
        aniSet.start();
    }

    private void doFloatDownAnimation() {
        for (Animator a : mAnimators) {
            a.cancel();
        }
        mAnimators.clear();

        ObjectAnimator xScaleAnim = ObjectAnimator.ofFloat(this, "scaleX", mCurrentScaleX, mInitialValue);
        ObjectAnimator yScaleAnim = ObjectAnimator.ofFloat(this, "scaleY", mCurrentScaleY, mInitialValue);
        xScaleAnim.setDuration(100);
        yScaleAnim.setDuration(100);
        xScaleAnim.addUpdateListener(mScaleXUpdateListener);
        yScaleAnim.addUpdateListener(mScaleYUpdateListener);

        ObjectAnimator upAnim = ObjectAnimator.ofFloat(this, "translationZ", mCurrentTranslationZ, 0);
        upAnim.setDuration(80);
        upAnim.addUpdateListener(mTranslationZUpdateListener);

        mAnimators.add(xScaleAnim);
        mAnimators.add(yScaleAnim);
        mAnimators.add(upAnim);
        AnimatorSet aniSet = new AnimatorSet();
        aniSet.playTogether(mAnimators);
        aniSet.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_OUTSIDE) {
            if (mOnPressAction != null) {
                mOnPressAction.onPressUp();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    class ScaleXUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleX = (Float) animation.getAnimatedValue("scaleX");
        }
    }

    class ScaleYUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            mCurrentScaleY = (Float) animation.getAnimatedValue("scaleY");
        }
    }

    class TranslationZUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float temp = (Float) animation.getAnimatedValue("translationZ");
            /** press down */
            if (temp == 0 && mCurrentTranslationZ > temp) {
                if (mOnPressAction != null) {
                    mOnPressAction.onPressDownFull();
                }
            }
            mCurrentTranslationZ = temp;
        }
    }
}
