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
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;

/**
 * Created by firemaples on 31/10/2016.
 */

public class ProgressView extends FloatingView {
    private TextView tv_progressMsg;
    private OnProgressViewCallback callback;

    public ProgressView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_progress_view;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    protected void setViews(View rootView) {
        tv_progressMsg = (TextView) rootView.findViewById(R.id.tv_progressMsg);
    }

    @Override
    public void attachToWindow() {
        super.attachToWindow();
        if (callback != null) {
            callback.onProgressViewAttachedToWindow();
        }
    }

    public void setCallback(OnProgressViewCallback callback) {
        this.callback = callback;
    }

    public void showMessage(String message) {
        if (!isAttached()) {
            attachToWindow();
        }
        tv_progressMsg.setText(message);
    }

    public interface OnProgressViewCallback {
        void onProgressViewAttachedToWindow();
    }
}
