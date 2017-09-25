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
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.FeatureUtil;

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

        if (!FeatureUtil.isNewModeEnabled()) {
            MenuItem item_changeMode = popupMenu.getMenu().findItem(R.id.menu_changeMode);
            item_changeMode.setVisible(false);
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
                    callback.onThanksItemClick();
                }
            } else if (itemId == R.id.menu_changeMode) {
                if (callback != null) {
                    callback.onChangeModeItemClick();
                }
            }
            return false;
        }
    };

    public interface OnFloatingBarMenuCallback {
        void onChangeModeItemClick();

        void onSettingItemClick();

        void onThanksItemClick();

        void onHideItemClick();

        void onCloseItemClick();
    }
}
