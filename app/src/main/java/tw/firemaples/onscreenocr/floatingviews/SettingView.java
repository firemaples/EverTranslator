package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by louis1chen on 04/11/2016.
 */

public class SettingView extends FloatingView {
    private Tool tool;

    public SettingView(Context context) {
        super(context);
        tool = Tool.getInstance();
        setViews();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_setting;
    }

    @Override
    protected int getLayoutGravity() {
        return Gravity.CENTER;
    }

    private void setViews() {
        CheckBox cb_debugMode = (CheckBox) getRootView().findViewById(R.id.cb_debugMode);
        getRootView().findViewById(R.id.bt_close).setOnClickListener(onClickListener);

        cb_debugMode.setChecked(tool.isDebugMode());

        cb_debugMode.setOnCheckedChangeListener(onCheckChangeListener);
    }

    private CompoundButton.OnCheckedChangeListener onCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int id = buttonView.getId();
            if (id == R.id.cb_debugMode) {
                tool.setDebugMode(isChecked);
            }
        }
    };

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_close) {
                SettingView.this.detachFromWindow();
            }
        }
    };
}
