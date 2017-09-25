package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by firemaples on 01/09/2017.
 */

public abstract class MovableFloatingView extends FloatingView {

    public MovableFloatingView(Context context) {
        super(context);
    }

    protected void setDragView(View view) {
        view.setOnTouchListener(onTouchListener);
    }

    private boolean isAlignParentLeft() {
        return (Gravity.getAbsoluteGravity(getLayoutGravity(), getRootView().getLayoutDirection()) & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT;
    }

    private boolean isAlignParentTop() {
        return (getLayoutGravity() & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP;
    }

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        private int initX, initY;
        private float initTouchX, initTouchY;
        private boolean hasMoved = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hasMoved = false;
                    initX = getFloatingLayoutParams().x;
                    initY = getFloatingLayoutParams().y;
                    initTouchX = event.getRawX();
                    initTouchY = event.getRawY();
//                    Tool.logInfo("Action down: initX" + initX + " initY:" + initY + " initTouchX:" + initTouchX + " initTouchY:" + initTouchY);
                    break;
                case MotionEvent.ACTION_UP:
                    if (hasMoved) {
                        return true;
                    }
                    hasMoved = false;
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
                    getWindowManager().updateViewLayout(getRootView(), getFloatingLayoutParams());
//                    Tool.logInfo("Touch location: x:" + event.getRawX() + " y:" + event.getRawY());
//                    Tool.logInfo("New location: x:" + nextX + " y:" + nextY);
                    break;
            }
            return false;
        }
    };
}
