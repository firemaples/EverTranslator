package tw.firemaples.onscreenocr.captureview.fullscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;

/**
 * Created by Firemaples on 2016/3/1.
 */
public class FullScreenCaptureAreaSelectionView extends ImageView {

    private boolean enable = true;

    private Point drawingStartPoint, drawingEndPoint;
    private Paint drawingLinePaint;

    private List<Rect> boxList = new ArrayList<>();
    private Paint boxPaint;

    private OnAreaSelectionViewCallback callback;

    private OnTouchListener onTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!enable) {
                return false;
            }
            Point point = new Point((int) event.getX(), (int) event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawingStartPoint = point;
                    break;
                case MotionEvent.ACTION_UP:
                    addBox(drawingStartPoint, point);
                    drawingStartPoint = drawingEndPoint = null;
                    invalidate();
                    if (callback != null) {
                        callback.onAreaSelected(FullScreenCaptureAreaSelectionView.this);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawingEndPoint = point;
                    invalidate();
                    break;
            }

            return true;
        }
    };

    public FullScreenCaptureAreaSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            this.iniView();
        }
    }

    private void iniView() {
        this.setOnTouchListener(onTouchListener);

        drawingLinePaint = new Paint();
        drawingLinePaint.setAntiAlias(true);
        drawingLinePaint.setColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewPaint_drawingLinePaint));
        drawingLinePaint.setStrokeWidth(10);

        boxPaint = new Paint();
        boxPaint.setAntiAlias(true);
        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewPaint_boxPaint));
        boxPaint.setStrokeWidth(6);
        boxPaint.setStyle(Paint.Style.STROKE);


//        enable();
    }

    public void setCallback(OnAreaSelectionViewCallback callback) {
        this.callback = callback;
    }

    //    public void enable() {
//        enable = true;
//        this.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewBackground_enable));
//    }
//
//    public void disable() {
//        enable = false;
//        this.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewBackground_disabled));
//    }

    public void clear() {
        drawingStartPoint = drawingEndPoint = null;
        boxList.clear();
        invalidate();
    }

    public List<Rect> getBoxList() {
        return boxList;
    }

    private void addBox(Point startPoint, Point endPoint) {
        boxList.add(new Rect(startPoint.x, startPoint.y, endPoint.x, endPoint.y));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            canvas.save();

            if (drawingStartPoint != null && drawingEndPoint != null) {
                canvas.drawLine(drawingStartPoint.x, drawingStartPoint.y, drawingEndPoint.x, drawingEndPoint.y, drawingLinePaint);
            }

            for (Rect box : boxList) {
                canvas.drawRect(box, boxPaint);
            }

            canvas.restore();
        }
    }

    public interface OnAreaSelectionViewCallback {
        void onAreaSelected(FullScreenCaptureAreaSelectionView areaSelectionView);
    }
}
