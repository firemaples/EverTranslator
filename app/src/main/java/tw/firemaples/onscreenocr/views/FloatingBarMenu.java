package tw.firemaples.onscreenocr.views;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.AppMode;
import tw.firemaples.onscreenocr.utils.SharePreferenceUtil;

/**
 * Created by firemaples on 31/10/2016.
 */

public class FloatingBarMenu {
    private PopupMenu popupMenu;
    private OnFloatingBarMenuCallback callback;

    public FloatingBarMenu(Context context, View anchor, OnFloatingBarMenuCallback callback) {
        this.callback = callback;

        popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(R.menu.menu_floating_bar);
        popupMenu.setOnMenuItemClickListener(onMenuItemClickListener);

        MenuItem item_changeMode = popupMenu.getMenu().findItem(R.id.menu_changeMode);
        AppMode currentAppMode = SharePreferenceUtil.getInstance().getAppMode();
        if (currentAppMode == AppMode.Normal) {
            item_changeMode.setTitle(R.string.toLiteMode);
        } else {
            item_changeMode.setTitle(R.string.toNormalMode);
        }
    }

    public void show() {
        popupMenu.show();
    }

    @SuppressWarnings("FieldCanBeLocal")
    private PopupMenu.OnMenuItemClickListener onMenuItemClickListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            popupMenu.dismiss();
            int itemId = item.getItemId();
            if (itemId == R.id.menu_close) {
                if (callback != null) {
                    callback.onCloseItemClick();
                }
            } else if (itemId == R.id.menu_setting) {
                if (callback != null) {
                    callback.onSettingItemClick();
                }
            } else if (itemId == R.id.menu_hide) {
                if (callback != null) {
                    callback.onHideItemClick();
                }
            } else if (itemId == R.id.menu_about) {
                if (callback != null) {
                    callback.onAboutItemClick();
                }
            } else if (itemId == R.id.menu_changeMode) {
                if (callback != null) {
                    if (SharePreferenceUtil.getInstance().getAppMode() == AppMode.Normal) {
                        callback.onChangeModeItemClick(AppMode.Lite);
                    } else {
                        callback.onChangeModeItemClick(AppMode.Normal);
                    }
                }
            } else if (itemId == R.id.menu_help) {
                if (callback != null) {
                    callback.onHelpClick();
                }
            }
            return false;
        }
    };

    public interface OnFloatingBarMenuCallback {
        void onChangeModeItemClick(AppMode toAppMode);

        void onSettingItemClick();

        void onAboutItemClick();

        void onHideItemClick();

        void onCloseItemClick();

        void onHelpClick();
    }
}
