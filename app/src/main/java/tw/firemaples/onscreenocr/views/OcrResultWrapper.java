/*
 * Copyright 2016-2017 Louis Chen [firemaples@gmail.com].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;

/**
 * Created by firemaples on 29/11/2016.
 */

public class OcrResultWrapper extends FrameLayout {
    private List<OcrResult> ocrResults = new ArrayList<>();

    private OcrResultWindow ocrResultWindow;

    public OcrResultWrapper(Context context, OcrResultWindow.OnOcrResultWindowCallback callback) {
        super(context);
        initViews(callback);
    }

    private void initViews(OcrResultWindow.OnOcrResultWindowCallback callback) {
        ocrResultWindow = new OcrResultWindow(getContext(), this, callback);
    }

    public void setOcrResults(List<OcrResult> ocrResults) {
        this.ocrResults.addAll(ocrResults);
        updateView();
    }

    public void clear() {
        ocrResults.clear();
        updateView();
    }

    private void updateView() {
        if (ocrResults.size() > 0) {
            for (OcrResult ocrResult : ocrResults) {
                Rect parentRect = ocrResult.getRect();

                for (Rect rect : ocrResult.getBoxRects()) {
                    ImageView ocrResultCover = new ImageView(getContext());

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

                if (SharePreferenceUtil.getInstance().isDebugMode()) {
                    bgImage.setImageBitmap(ocrResult.getDebugInfo().getCroppedBitmap());
                }

                this.addView(bgImage, 0);

                ocrResultWindow.setOcrResult(ocrResult);
                ocrResultWindow.show(bgImage);
            }
        } else {
            this.removeAllViews();

            ocrResultWindow.dismiss();
        }
    }
}
