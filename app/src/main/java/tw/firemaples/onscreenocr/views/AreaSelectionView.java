package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.tts.AndroidTTSManager;

/**
 * Created by Firemaples on 2016/3/1.
 */
public class AreaSelectionView extends ImageView {
    private static final Logger logger = LoggerFactory.getLogger(AreaSelectionView.class);

    private static final long TIME_BORDER_ANIM = 1000;
    private static final long INTERVAL_BORDER_ANIM = 10;

    private static final long DELAY_TEXT_HIDDEN_ANIM = 2000;
    private static final long TIME_TEXT_HIDDEN_ANIM = 800;
    private static final long INTERVAL_TEXT_HIDDEN_ANIM = 10;

    private boolean enable = true;

    private Point drawingStartPoint, drawingEndPoint;
    private Paint drawingLinePaint;

    private List<Rect> boxList = new ArrayList<>();
    private Paint boxPaint;

    private CountDownTimer timer, redrawHelpTextTimer;
    private int borderAnimationProgress = 0;
    private Paint borderPaint;

    private Paint helpTextPaint, helpTextAlphaPaint;

    private int maxRectCount = 0;

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
                    if (maxRectCount > 0 && maxRectCount == boxList.size()) {
                        if (maxRectCount == 1) {
                            boxList.clear();
                        } else {
                            return false;
                        }
                    }
                    drawingStartPoint = point;
                    break;
                case MotionEvent.ACTION_UP:
                    if (drawingEndPoint != null) {
                        addBox(drawingStartPoint, point);
                        drawingStartPoint = drawingEndPoint = null;
                        invalidate();
                        startRedrawHintHiddenCountDown();
                        if (callback != null) {
                            callback.onAreaSelected(AreaSelectionView.this);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (drawingStartPoint != null) {
                        drawingEndPoint = point;
                        invalidate();
                    }
                    break;
            }

            return true;
        }
    };

    public AreaSelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            this.iniView();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopRedrawHintHiddenCountDown();
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

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(12);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewPaint_borderPaint));

        helpTextPaint = new Paint();
        helpTextPaint.setAntiAlias(true);
        helpTextPaint.setTextAlign(Paint.Align.CENTER);
        helpTextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.areaSelectionView_helpTextSize));
        helpTextPaint.setColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewPaint_helpTextPaint));

        helpTextAlphaPaint = new Paint(helpTextPaint);
        helpTextAlphaPaint.setAlpha(0);

//        enable();
    }

    public void setCallback(OnAreaSelectionViewCallback callback) {
        this.callback = callback;
    }

    public void setMaxRectCount(int maxRectCount) {
        this.maxRectCount = maxRectCount;
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

    public void setBoxList(List<Rect> boxList) {
        if (boxList != null) {
            this.boxList = boxList;
        } else {
            this.boxList.clear();
        }
        invalidate();
        if (boxList.size() > 0) {
            startRedrawHintHiddenCountDown();
            callback.onAreaSelected(this);
        }
    }

    private void addBox(Point startPoint, Point endPoint) {
        boxList.add(getNewBox(startPoint, endPoint));
    }

    private Rect getNewBox(Point startPoint, Point endPoint) {
        int x1 = startPoint.x, x2 = endPoint.x, y1 = startPoint.y, y2 = endPoint.y;
        int left, top, right, bottom;

        left = Math.min(x1, x2);
        right = Math.max(x1, x2);
        top = Math.min(y1, y2);
        bottom = Math.max(y1, y2);

        return new Rect(left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            canvas.save();

            drawHelpText(canvas);

            if (drawingStartPoint != null && drawingEndPoint != null) {
//                canvas.drawLine(drawingStartPoint.x, drawingStartPoint.y, drawingEndPoint.x, drawingEndPoint.y, drawingLinePaint);
                canvas.drawRect(getNewBox(drawingStartPoint, drawingEndPoint), drawingLinePaint);
            }

            for (Rect box : boxList) {
                canvas.drawRect(box, boxPaint);
            }

            drawBorder(canvas, borderAnimationProgress);

            canvas.restore();
        }
    }

    public void startBorderAnimation() {
        borderAnimationProgress = 0;
        stopBorderAnimation();

        timer = new CountDownTimer(TIME_BORDER_ANIM, INTERVAL_BORDER_ANIM) {
            @Override
            public void onTick(long millisUntilFinished) {
                borderAnimationProgress = (int) (((float) (TIME_BORDER_ANIM - millisUntilFinished) / (float) TIME_BORDER_ANIM) * 100f);
//                logger.info("borderAnimationProgress: " + borderAnimationProgress);
                invalidate();
            }

            @Override
            public void onFinish() {
                borderAnimationProgress = 100;
//                logger.info("borderAnimationProgress: finished");
                invalidate();
            }
        }.start();
    }

    public void stopBorderAnimation() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void drawBorder(Canvas canvas, int progress) {
//        logger.info("drawBorder: progress: " + progress);
        int topSteps = 25;
        int centerSteps = 50;
        int bottomSteps = 25;

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float topRunLength;
        float topXStart = width / 2;
        if (progress >= topSteps) {
            topRunLength = width / 2;
        } else {
            topRunLength = ((float) width / 2f / (float) topSteps * (float) progress);
        }
        //draw top-right
        canvas.drawLine(topXStart, 0, topXStart + topRunLength, 0, borderPaint);
        //draw top-left
        canvas.drawLine(topXStart, 0, topXStart - topRunLength, 0, borderPaint);

        if (progress > topSteps) {
            float leftRightRunLength;
            float leftRightYStartY = 0;
            if (progress >= topSteps + centerSteps) {
                //noinspection SuspiciousNameCombination
                leftRightRunLength = height;
            } else {
                leftRightRunLength = (float) height / (float) centerSteps * (float) (progress - topSteps);
            }
            //draw left
            canvas.drawLine(0, leftRightYStartY, 0, leftRightYStartY + leftRightRunLength, borderPaint);
            //draw right
            canvas.drawLine(width, leftRightYStartY, width, leftRightYStartY + leftRightRunLength, borderPaint);
        }

        if (progress > topSteps + centerSteps) {
            float bottomRunLength;
            float bottomLeftXStart = 0;
            @SuppressWarnings("UnnecessaryLocalVariable") float bottomRightXStart = width;

            if (progress == 100) {
                bottomRunLength = width / 2;
            } else {
                bottomRunLength = ((float) width / 2f / (float) bottomSteps * (float) (progress - topSteps - centerSteps));
            }
            //draw bottom-right
            canvas.drawLine(bottomRightXStart, height, bottomRightXStart - bottomRunLength, height, borderPaint);
            //draw bottom-left
            canvas.drawLine(bottomLeftXStart, height, bottomLeftXStart + bottomRunLength, height, borderPaint);
        }
    }

    private void drawHelpText(Canvas canvas) {
        //Do not show when drawing
        if (drawingStartPoint != null || drawingEndPoint != null) {
            return;
        }

        String message;
        Paint textPaint;
        if (boxList.size() > 0) {
            message = getContext().getString(R.string.redrawAnAreaForTranslation);
            textPaint = helpTextAlphaPaint;
        } else {
            message = getContext().getString(R.string.drawAnAreaForTranslation);
            textPaint = helpTextPaint;
        }

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(message, xPos, yPos, textPaint);
    }

    private void startRedrawHintHiddenCountDown() {
        stopRedrawHintHiddenCountDown();
        redrawHelpTextTimer = new CountDownTimer(TIME_TEXT_HIDDEN_ANIM + DELAY_TEXT_HIDDEN_ANIM, INTERVAL_TEXT_HIDDEN_ANIM) {
            int MAX_ALPHA = 200;

            @Override
            public void onTick(long millisUntilFinished) {
                int alpha;
                if (millisUntilFinished < TIME_TEXT_HIDDEN_ANIM) {
                    alpha = (int) ((float) millisUntilFinished / (float) TIME_TEXT_HIDDEN_ANIM * (float) MAX_ALPHA);
                    logger.info("Alpha: " + alpha);
                } else {
                    alpha = MAX_ALPHA;
                    logger.info("Alpha: fixed " + alpha);
                }
                helpTextAlphaPaint.setAlpha(alpha);
//                logger.info("borderAnimationProgress: " + borderAnimationProgress);
                invalidate();
            }

            @Override
            public void onFinish() {
                helpTextAlphaPaint.setAlpha(0);
//                logger.info("borderAnimationProgress: finished");
                invalidate();
            }
        }.start();
    }

    private void stopRedrawHintHiddenCountDown() {
        if (redrawHelpTextTimer != null) {
            redrawHelpTextTimer.cancel();
            redrawHelpTextTimer = null;
        }
    }

    public interface OnAreaSelectionViewCallback {
        void onAreaSelected(AreaSelectionView areaSelectionView);
    }
}
