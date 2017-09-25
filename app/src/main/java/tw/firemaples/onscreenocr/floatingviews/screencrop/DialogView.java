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
import android.widget.Button;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;

/**
 * Created by firemaples on 22/11/2016.
 */

public class DialogView extends FloatingView {
    private TextView tv_dialogTitle, tv_dialogContent;
    private Button bt_dialogOk, bt_dialogCancel;

    private OnDialogViewCallback callback = new OnDialogViewCallback();

    public DialogView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_dialog;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void setViews(View rootView) {
        tv_dialogTitle = (TextView) rootView.findViewById(R.id.tv_dialogTitle);
        tv_dialogContent = (TextView) rootView.findViewById(R.id.tv_dialogContent);

        bt_dialogOk = (Button) rootView.findViewById(R.id.bt_dialogOk);
        bt_dialogCancel = (Button) rootView.findViewById(R.id.bt_dialogCancel);

        bt_dialogOk.setOnClickListener(onClickListener);
        bt_dialogCancel.setOnClickListener(onClickListener);
    }

    public void setType(Type type) {
        switch (type) {
            case CONFIRM_ONLY:
                bt_dialogOk.setVisibility(View.VISIBLE);
                bt_dialogCancel.setVisibility(View.GONE);
                break;
            case CANCEL_ONLY:
                bt_dialogOk.setVisibility(View.GONE);
                bt_dialogCancel.setVisibility(View.VISIBLE);
                break;
            case CONFIRM_CANCEL:
                bt_dialogOk.setVisibility(View.VISIBLE);
                bt_dialogCancel.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void reset() {
        callback = new OnDialogViewCallback();
        bt_dialogOk.setText(android.R.string.ok);
        bt_dialogCancel.setText(android.R.string.cancel);
    }

    public void setCallback(OnDialogViewCallback callback) {
        this.callback = callback;
    }

    public void setTitle(String title) {
        tv_dialogTitle.setText(title);
    }

    public void setContentMsg(String msg) {
        tv_dialogContent.setText(msg);
    }

    public Button getOkBtn() {
        return bt_dialogOk;
    }

    public Button getCancelBtn() {
        return bt_dialogCancel;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_dialogOk) {
                callback.OnConfirmClick(DialogView.this);
            } else if (id == R.id.bt_dialogCancel) {
                callback.onCancelClicked(DialogView.this);
            }
        }
    };

    public enum Type {
        CONFIRM_ONLY, CANCEL_ONLY, CONFIRM_CANCEL
    }

    public static class OnDialogViewCallback {
        public void OnConfirmClick(DialogView dialogView) {
            dialogView.detachFromWindow();
        }

        public void onCancelClicked(DialogView dialogView) {
            dialogView.detachFromWindow();
        }
    }
}
