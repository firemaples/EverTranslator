package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.screencrop.OcrResultView;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 29/11/2016.
 */

public class OcrResultWrapper extends RelativeLayout {
    private Paint borderPaint;

    private OcrResultView.OcrNTranslateState state;
    private List<OcrResult> ocrResultList = new ArrayList<>();

    private OcrResultWindow ocrResultWindow;

    public OcrResultWrapper(Context context, OcrResultWindow.OnOcrResultWindowCallback callback) {
        super(context);
        initViews(callback);
    }

    private void initViews(OcrResultWindow.OnOcrResultWindowCallback callback) {
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.orcResultBorder_color));

        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.captureAreaSelectionViewBackground_enable));

        ocrResultWindow = new OcrResultWindow(getContext(), this, callback);
    }

    public void updateViewState(OcrResultView.OcrNTranslateState state, List<OcrResult> ocrResultList) {
        this.state = state;
        this.ocrResultList = ocrResultList;
        updateView();
    }

    public void clear() {
        this.ocrResultList.clear();
        updateView();
    }

    private void updateView() {
        if (ocrResultList.size() > 0) {
            this.removeAllViews();
            for (OcrResult ocrResult : ocrResultList) {

                Rect parentRect = ocrResult.getRect();

                for (Rect rect : ocrResult.getBoxRects()) {
                    ImageView ocrResultCover = new ImageView(getContext());

                    Tool.logInfo(String.format(Locale.getDefault(), "parentRect:[%d,%d], rect[%d,%d]", parentRect.left, parentRect.top, rect.left, rect.top));

                    LayoutParams layoutParams = new LayoutParams(rect.width(), rect.height());
                    layoutParams.setMargins(parentRect.left + rect.left, parentRect.top + rect.top, 0, 0);
                    ocrResultCover.setLayoutParams(layoutParams);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ocrResultCover.setBackgroundColor(getResources().getColor(R.color.ocrResultCover_color, null));
                    } else {
                        ocrResultCover.setBackgroundColor(getResources().getColor(R.color.ocrResultCover_color));
                    }

                    this.addView(ocrResultCover);
                }

                ImageView bgImage = new ImageView(getContext());

                LayoutParams layoutParams = new LayoutParams(parentRect.width(), parentRect.height());
                layoutParams.setMargins(parentRect.left, parentRect.top, 0, 0);
                bgImage.setLayoutParams(layoutParams);

                if (SharePreferenceUtil.getInstance().isDebugMode() && ocrResult.getDebugInfo() != null) {
                    bgImage.setImageBitmap(ocrResult.getDebugInfo().getCroppedBitmap());
                }

                this.addView(bgImage, 0);

                ocrResultWindow.setOcrResult(state, ocrResult);
                ocrResultWindow.show(bgImage);
            }
        } else {
            this.removeAllViews();

            ocrResultWindow.dismiss();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), borderPaint);
    }
}
