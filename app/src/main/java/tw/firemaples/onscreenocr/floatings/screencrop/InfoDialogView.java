package tw.firemaples.onscreenocr.floatings.screencrop;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatings.FloatingView;
import tw.firemaples.onscreenocr.utils.HomeWatcher;

/**
 * Created by louis1chen on 26/03/2018.
 */

public abstract class InfoDialogView extends FloatingView {
    public static final int MODE_OK_CANCEL = 0;
    public static final int MODE_CLOSE = 1;

    public InfoDialogView(Context context) {
        super(context);
    }

    @Override
    public void onViewStart() {
        super.onViewStart();
        setViews(getRootView());
        setupHomeButtonWatcher(onHomePressedListener);
    }

    @Override
    protected final int getLayoutId() {
        return R.layout.view_info_dialog;
    }

    @Override
    protected final int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    abstract int getButtonMode();

    abstract int getContentLayoutId();

    abstract String getTitle();

    protected void setViews(View rootView) {
        TextView tv_title = (TextView) getRootView().findViewById(R.id.tv_infoDialogTitle);
        tv_title.setText(getTitle());

        ViewGroup contentWrapper = (ViewGroup) rootView.findViewById(R.id.wrapper_infoDialogContent);
        contentWrapper.addView(
                View.inflate(getContext(), getContentLayoutId(), null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setupButtons();
    }

    private void setupButtons() {
        Button btnOk = getButtonOk();
        Button btnCancel = getButtonCancel();

        switch (getButtonMode()) {
            case MODE_OK_CANCEL:
                btnOk.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;
            case MODE_CLOSE:
                btnOk.setVisibility(View.VISIBLE);
                btnOk.setText(R.string.btn_close);
                btnCancel.setVisibility(View.GONE);
                break;
        }

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
    }

    protected Button getButtonOk() {
        return (Button) getRootView().findViewById(R.id.bt_dialogOk);
    }

    protected Button getButtonCancel() {
        return (Button) getRootView().findViewById(R.id.bt_dialogCancel);
    }

    @Override
    public boolean onBackButtonPressed() {
        detachFromWindow();
        return true;
    }

    private HomeWatcher.OnHomePressedListener onHomePressedListener = new HomeWatcher.OnHomePressedListener() {
        @Override
        public void onHomePressed() {
            onBackButtonPressed();
        }

        @Override
        public void onHomeLongPressed() {

        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_dialogOk) {
                onButtonOkClicked();
            } else if (id == R.id.bt_dialogCancel) {
                onButtonCancelClicked();
            }
        }
    };

    protected void onButtonOkClicked() {
        detachFromWindow();
    }

    protected void onButtonCancelClicked() {
        detachFromWindow();
    }
}
