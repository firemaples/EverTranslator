package tw.firemaples.onscreenocr.floatings;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tw.firemaples.onscreenocr.utils.UIUtil;

/**
 * Created by firemaples on 01/09/2017.
 */

public abstract class MovableFloatingView extends FloatingView {
    private static final Logger logger = LoggerFactory.getLogger(MovableFloatingView.class);

    private static final float MOVE_TO_EDGE_OVERSHOOT_TENSION = 1.25f;
    private static final long MOVE_TO_EDGE_DURATION = 450L;
    private static final int MOVE_TO_EDGE_MARGIN_DP = -20;

    private float fromAlpha = 1.0f;
    private static final float DESTINATION_ALPHA = 0.2f;
    private static final long FADE_OUT_ANIM_DURATION = 800L;
    private static final long FADE_OUT_ANIM_DELAY = 1000L;

    private View dragView;

    private ValueAnimator mMoveEdgeAnimator;
    private final TimeInterpolator mMoveEdgeInterpolator;
    private ValueAnimator mFadeOutAnimator;

    private TouchInterceptor touchInterceptor;

    public MovableFloatingView(Context context) {
        super(context);
        mMoveEdgeInterpolator = new OvershootInterpolator(MOVE_TO_EDGE_OVERSHOOT_TENSION);
    }

    protected void setDragView(View view) {
        this.dragView = view;

        this.fromAlpha = view.getAlpha();
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnTouchListener(onTouchListener);
    }

    @Override
    public void attachToWindow() {
        super.attachToWindow();
        moveToEdgeOrFadeOut();
    }

    @Override
    protected boolean canMoveOutside() {
        return enableAutoMoveToEdge();
    }

    protected boolean enableAutoMoveToEdge() {
        return false;
    }

    protected boolean enableTransparentWhenMoved() {
        return false;
    }

    protected int[] getEdgeDistance() {
        int[] edgePosition = getEdgePosition(getFloatingLayoutParams().x, getFloatingLayoutParams().y);

        return new int[]{Math.abs(edgePosition[0] - getFloatingLayoutParams().x),
                Math.abs(edgePosition[1] - getFloatingLayoutParams().y)};
    }

    private void moveToEdge() {
        int[] edgePosition = getEdgePosition(getFloatingLayoutParams().x, getFloatingLayoutParams().y);

        moveTo(getFloatingLayoutParams().x, getFloatingLayoutParams().y,
                edgePosition[0], edgePosition[1], true);
    }

    private int[] getEdgePosition(int currentX, int currentY) {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int viewWidth = getRootView().getWidth();

        int viewCenterX = currentX + viewWidth / 2;

        int margin = (int) UIUtil.INSTANCE.dpToPx(getContext(), MOVE_TO_EDGE_MARGIN_DP);
        int edgeX;
        if (viewCenterX < metrics.widthPixels / 2) {
            // near left
            edgeX = margin;
        } else {
            // near right
            edgeX = metrics.widthPixels - viewWidth - margin;
        }

        return new int[]{edgeX, currentY};
    }

    /**
     * @return 1 when near right edge, -1 when near left edge
     */
    protected int isNearTo() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);

        int viewWidth = getRootView().getWidth();

        int viewCenterX = getFloatingLayoutParams().x + viewWidth / 2;

        int fixCode = isAlignParentLeft() ? 1 : -1;

        if (viewCenterX < metrics.widthPixels / 2) {
            // near left
            return -1 * fixCode;
        } else {
            // near right
            //noinspection PointlessArithmeticExpression
            return 1 * fixCode;
        }
    }

    private void moveTo(int currentX, int currentY, int goalPositionX, int goalPositionY, boolean withAnimation) {
        if (withAnimation) {
            // TO-DO:Y座標もアニメーションさせる

            //to move only y-coord
            if (goalPositionX == currentX) {
                mMoveEdgeAnimator = ValueAnimator.ofInt(currentY, goalPositionY);
                mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        getFloatingLayoutParams().y = (Integer) animation.getAnimatedValue();
                        updateViewLayout();
//                        updateInitAnimation(animation);
                    }
                });
            } else {
                // to move only x coord (to left or right)
                getFloatingLayoutParams().y = goalPositionY;
                mMoveEdgeAnimator = ValueAnimator.ofInt(currentX, goalPositionX);
                mMoveEdgeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        getFloatingLayoutParams().x = (Integer) animation.getAnimatedValue();
                        updateViewLayout();
//                        updateInitAnimation(animation);
                    }
                });
            }
            // X軸のアニメーション設定
            mMoveEdgeAnimator.setDuration(MOVE_TO_EDGE_DURATION);
            mMoveEdgeAnimator.setInterpolator(mMoveEdgeInterpolator);
            mMoveEdgeAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (enableTransparentWhenMoved()) {
                        fadeOut();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            mMoveEdgeAnimator.start();
        } else {
            // 位置が変化した時のみ更新
            if (getFloatingLayoutParams().x != goalPositionX || getFloatingLayoutParams().y != goalPositionY) {
                getFloatingLayoutParams().x = goalPositionX;
                getFloatingLayoutParams().y = goalPositionY;
                updateViewLayout();
            }
        }
    }

    private boolean isAlignParentLeft() {
        return (Gravity.getAbsoluteGravity(getLayoutGravity(), getRootView().getLayoutDirection()) & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT;
    }

    private boolean isAlignParentTop() {
        return (getLayoutGravity() & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP;
    }

    private void fadeOut() {
        if (mFadeOutAnimator != null) {
            mFadeOutAnimator.cancel();
        }
        mFadeOutAnimator = ValueAnimator.ofFloat(fromAlpha, DESTINATION_ALPHA);
        mFadeOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                getRootView().setAlpha((Float) animation.getAnimatedValue());
            }
        });
        mFadeOutAnimator.setDuration(FADE_OUT_ANIM_DURATION);
        mFadeOutAnimator.setStartDelay(FADE_OUT_ANIM_DELAY);
        mFadeOutAnimator.start();
    }

    private boolean moveToEdgeOrFadeOut() {
        if (enableAutoMoveToEdge()) {
            moveToEdge();
            return true;
        } else if (enableTransparentWhenMoved()) {
            fadeOut();
            return true;
        }
        return false;
    }

    protected void rescheduleFadeOut() {
        if (mFadeOutAnimator != null) {
            mFadeOutAnimator.cancel();
        }
        getRootView().setAlpha(fromAlpha);
        if (enableTransparentWhenMoved()) {
            fadeOut();
        }
    }

    public void setTouchInterceptor(TouchInterceptor touchInterceptor) {
        this.touchInterceptor = touchInterceptor;
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private int initX, initY;
        private float initTouchX, initTouchY;
        private boolean hasMoved = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mFadeOutAnimator != null && mFadeOutAnimator.isRunning()) {
                mFadeOutAnimator.cancel();
            }
            if (enableTransparentWhenMoved()) {
                getRootView().setAlpha(fromAlpha);
            }

            if (touchInterceptor != null && touchInterceptor.onTouch(v, event, hasMoved)) {
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hasMoved = false;
                    initX = getFloatingLayoutParams().x;
                    initY = getFloatingLayoutParams().y;
                    initTouchX = event.getRawX();
                    initTouchY = event.getRawY();
//                    logger.info("Action down: initX" + initX + " initY:" + initY + " initTouchX:" + initTouchX + " initTouchY:" + initTouchY);
                    break;
                case MotionEvent.ACTION_UP:
                    boolean temp = hasMoved;
                    hasMoved = false;
                    moveToEdgeOrFadeOut();
                    if (temp) {
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(initTouchX - event.getRawX()) > 20 || Math.abs(initTouchY - event.getRawY()) > 20) {
                        hasMoved = true;
                    }
                    int xDiff = (int) (event.getRawX() - initTouchX);
                    int yDiff = (int) (event.getRawY() - initTouchY);

                    //TODO Not working when align parent center
                    int nextX = initX + (isAlignParentLeft() ? xDiff : -xDiff);
                    int nextY = initY + (isAlignParentTop() ? yDiff : -yDiff);

                    if (nextX < 0) {
                        nextX = 0;
                    }
                    if (nextY < 0) {
                        nextY = 0;
                    }
                    getFloatingLayoutParams().x = nextX;
                    getFloatingLayoutParams().y = nextY;
                    updateViewLayout();
//                    logger.info("Touch location: x:" + event.getRawX() + " y:" + event.getRawY());
//                    logger.info("New location: x:" + nextX + " y:" + nextY);
                    break;
            }
            return false;
        }
    };
}

interface TouchInterceptor {
    boolean onTouch(View v, MotionEvent event, boolean hasMoved);
}