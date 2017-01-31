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
