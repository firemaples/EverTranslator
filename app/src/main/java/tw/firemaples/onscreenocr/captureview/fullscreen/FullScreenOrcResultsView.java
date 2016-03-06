package tw.firemaples.onscreenocr.captureview.fullscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.List;

import tw.firemaples.onscreenocr.orc.OrcResult;

/**
 * Created by firem_000 on 2016/3/6.
 */
public class FullScreenOrcResultsView extends ImageView {

    private List<OrcResult> orcResults;

    private Paint boxPaint;

    private TextPaint textPaint;

    private int textBackgroundPadding = 5;
    private DisplayMetrics metrics;

    public FullScreenOrcResultsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            init();
        }
    }

    private void init() {
        boxPaint = new Paint();
        boxPaint.setStrokeWidth(20);
        boxPaint.setColor(Color.WHITE);
        boxPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLUE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(96);

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        metrics = new DisplayMetrics();
        display.getMetrics(metrics);
    }

    public void setOrcResults(List<OrcResult> orcResults) {
        this.orcResults = orcResults;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isInEditMode()) {
            for (OrcResult orcResult : orcResults) {
                if (orcResult.getSubRect() != null) {
                    Rect subRect = orcResult.getSubRect();

                    int textWidth = Math.max(subRect.width(), (int) textPaint.measureText(orcResult.getTranslatedText()));
                    textWidth = Math.min(textWidth, metrics.widthPixels - subRect.left - 16);
                    int textHeight = Math.max(subRect.height(), (int) textPaint.getTextSize());

                    canvas.drawRect(new Rect(subRect.left, subRect.top, subRect.left + textWidth, subRect.top + textHeight), boxPaint);

                    StaticLayout sl = new StaticLayout(orcResult.getTranslatedText(), textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1, 1, false);

                    canvas.save();
                    canvas.translate(subRect.left, subRect.top);
                    sl.draw(canvas);
                    canvas.restore();

//                    canvas.drawText(orcResult.getTranslatedText(), subRect.left, subRect.bottom, textPaint);
                }
            }
        }
    }
}
