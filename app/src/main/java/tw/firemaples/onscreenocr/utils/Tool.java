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

package tw.firemaples.onscreenocr.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;

import tw.firemaples.onscreenocr.BuildConfig;

/**
 * Created by firemaples on 2016/3/1.
 */
public class Tool {
    private static Tool _instance;

    private static String LOG_TAG = "OnScreenOcr";

    private Context context;

    public static void init(Context context) {
        _instance = new Tool(context);
    }

    public static Tool getInstance() {
        return _instance;
    }

    private Tool(Context context) {
        this.context = context;
    }

    public static Context getContext() {
        if (getInstance() == null) {
            throw new IllegalStateException("Instance of tool object has not been created");
        }
        return getInstance().context;
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void logError(String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(LOG_TAG, msg);
        }
    }

    public static void logInfo(String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(LOG_TAG, msg);
        }
    }

    public void showMsg(String msg) {
        if (getContext() == null) {
            return;
        }
        SuperToast.cancelAllSuperToasts();
        SuperToast.create(getContext(), msg, SuperToast.Duration.VERY_SHORT,
                Style.getStyle(Style.BLACK, SuperToast.Animations.FADE)).show();
    }

    public void showErrorMsg(String msg) {
        if (getContext() == null) {
            return;
        }
        SuperToast.cancelAllSuperToasts();
        SuperToast.create(getContext(), msg, SuperToast.Duration.VERY_SHORT,
                Style.getStyle(Style.RED, SuperToast.Animations.FADE)).show();
    }

    public static String replaceAllLineBreaks(String str, String replaceWith) {
        return str.replace("\r\n", replaceWith).replace("\r", replaceWith).replace("\n", replaceWith);
    }

    public void openBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            getContext().startActivity(intent);
        }
    }
}
