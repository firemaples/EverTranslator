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

package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.ocr.OcrResult;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;
import tw.firemaples.onscreenocr.views.OcrResultWindow;
import tw.firemaples.onscreenocr.views.OcrResultWrapper;

/**
 * Created by firemaples on 01/11/2016.
 */

public class OcrResultView extends FloatingView {
    private OcrResultWrapper view_ocrResultWrapper;
    private OcrResultWindow.OnOcrResultWindowCallback onOcrResultWindowCallback;

    public OcrResultView(Context context, OcrResultWindow.OnOcrResultWindowCallback onOcrResultWindowCallback) {
        super(context);
        this.onOcrResultWindowCallback = onOcrResultWindowCallback;
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_result_view;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected boolean fullScreenMode() {
        return true;
    }

    private void setViews(View rootView) {
        view_ocrResultWrapper = new OcrResultWrapper(getContext(), onOcrResultWindowCallback);
        ((ViewGroup) rootView).addView(view_ocrResultWrapper, 0, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setOcrResults(List<OcrResult> results) {
        view_ocrResultWrapper.setOcrResults(results);

        if (SharePreferenceUtil.getInstance().isDebugMode() && results.size() > 0) {
            setDebugInfo(results.get(0));
        }
    }

    private void setDebugInfo(OcrResult ocrResult) {
        TextView tv_debugInfo = (TextView) getRootView().findViewById(R.id.tv_debugInfo);
        String[] infoArray = ocrResult.getDebugInfo().getInfoList().toArray(new String[ocrResult.getDebugInfo().getInfoList().size()]);
        String debugInfoString = TextUtils.join("\n", infoArray);
        tv_debugInfo.setText(debugInfoString);
        tv_debugInfo.setVisibility(View.VISIBLE);
    }

    public void clear() {
        view_ocrResultWrapper.clear();
    }

    @Override
    public void detachFromWindow() {
        clear();
        super.detachFromWindow();
    }
}
