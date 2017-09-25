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

package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.MainActivity;
import tw.firemaples.onscreenocr.ScreenTranslatorService;
import tw.firemaples.onscreenocr.utils.PermissionUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by firemaples on 23/10/2016.
 */

public abstract class FloatingView {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams floatingLayoutParams;
    private boolean isAttached = false;
    private View rootView;

    public FloatingView(Context context) {
        this.context = context;
        rootView = LayoutInflater.from(context).inflate(getLayoutId(), null);
        windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        floatingLayoutParams = new WindowManager.LayoutParams(
                getLayoutSize(),
                getLayoutSize(),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        if (fullScreenMode()) {
            floatingLayoutParams.flags = floatingLayoutParams.flags | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        }
        floatingLayoutParams.gravity = getLayoutGravity();
    }

    protected abstract int getLayoutId();

    protected int getLayoutSize() {
        return WindowManager.LayoutParams.WRAP_CONTENT;
    }

    protected int getLayoutGravity() {
        return Gravity.TOP | Gravity.LEFT;
    }

    protected boolean fullScreenMode() {
        return false;
    }

    public Context getContext() {
        return context;
    }

    public View getRootView() {
        return rootView;
    }

    public WindowManager.LayoutParams getFloatingLayoutParams() {
        return floatingLayoutParams;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public void attachToWindow() {
        if (!isAttached) {
            if (PermissionUtil.checkDrawOverlayPermission(context)) {
                windowManager.addView(rootView, floatingLayoutParams);
                isAttached = true;
            } else {
                MainActivity.start(context);
                ScreenTranslatorService.stop(true);
            }
        }
    }

    public void detachFromWindow() {
        if (isAttached) {
            windowManager.removeView(rootView);
            isAttached = false;
        }
    }
}
