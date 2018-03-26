package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.utils.HomeWatcher;

/**
 * Created by louis1chen on 26/03/2018.
 */

public abstract class InfoDialogView extends FloatingView {

    public InfoDialogView(Context context) {
        super(context);
        setViews(getRootView());
        setupHomeButtonWatcher(onHomePressedListener);
    }

    @Override
    protected boolean layoutFocusable() {
        return true;
    }

    @Override
    protected final int getLayoutId() {
        return R.layout.view_info_dialog;
    }

    @Override
    protected final int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    abstract int getContentLayoutId();

    abstract String getTitle();

    protected void setViews(View rootView) {
        TextView tv_title = (TextView) getRootView().findViewById(R.id.tv_infoDialogTitle);
        tv_title.setText(getTitle());

        rootView.findViewById(R.id.bt_dialogClose).setOnClickListener(onClickListener);

        ViewGroup contentWrapper = (ViewGroup) rootView.findViewById(R.id.wrapper_infoDialogContent);
        contentWrapper.addView(
                View.inflate(getContext(), getContentLayoutId(), null),
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
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
            if (id == R.id.bt_dialogClose) {
                InfoDialogView.this.detachFromWindow();
            }
        }
    };
}
