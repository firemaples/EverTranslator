package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;

/**
 * Created by firemaples on 23/04/2017.
 */

public class TextEditDialogView extends FloatingView {
    private TextView tv_dialogTitle;
    private EditText et_text_edit;
    private Button bt_dialogOk, bt_dialogCancel;

    private OnTextEditDialogViewCallback callback = new OnTextEditDialogViewCallback();

    public TextEditDialogView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected boolean layoutFocusable() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_text_edit_dialog;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void setViews(View rootView) {
        tv_dialogTitle = (TextView) rootView.findViewById(R.id.tv_dialogTitle);
        et_text_edit = (EditText) rootView.findViewById(R.id.et_text_edit);

        bt_dialogOk = (Button) rootView.findViewById(R.id.bt_dialogOk);
        bt_dialogCancel = (Button) rootView.findViewById(R.id.bt_dialogCancel);

        bt_dialogOk.setOnClickListener(onClickListener);
        bt_dialogCancel.setOnClickListener(onClickListener);
    }

    @Override
    public boolean onBackButtonPressed() {
        callback.onCancelClicked(this);
        return true;
    }

    public void setCallback(OnTextEditDialogViewCallback callback) {
        this.callback = callback;
    }

    public void setTitle(String title) {
        tv_dialogTitle.setText(title);
    }

    public void setContentText(String text) {
        et_text_edit.setText(text);
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
                callback.OnConfirmClick(TextEditDialogView.this, et_text_edit.getText().toString());
            } else if (id == R.id.bt_dialogCancel) {
                callback.onCancelClicked(TextEditDialogView.this);
            }
        }
    };

    public static class OnTextEditDialogViewCallback {
        public void OnConfirmClick(TextEditDialogView textEditDialogView, String text) {
            textEditDialogView.detachFromWindow();
        }

        public void onCancelClicked(TextEditDialogView textEditDialogView) {
            textEditDialogView.detachFromWindow();
        }
    }
}
