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

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * This class is made for target view is not shown on screen when fragment or activity launched, trigger global layout changed event and recheck target view showing status.
 *
 * Created by Firemaples on 9/10/16.
 */
public class ViewPreparedWaiter {

    private View viewToWaitingFor;
    private OnViewPrepared callback;
    private boolean waiting = false;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (isShownOnScreen()) {
                stopWait();
                Log.i("ViewPreparedWaiter","View prepared");
                callback.onViewPrepared(viewToWaitingFor);
            }
        }
    };

    public void waitView(@NonNull View viewToWait, @NonNull final OnViewPrepared callback) {
        if (isWaiting()) {
            Log.i("ViewPreparedWaiter","Is waiting now, return");
            return;
        }

        this.viewToWaitingFor = viewToWait;
        this.callback = callback;

        waiting = true;

        if (isShownOnScreen()) {
            Log.i("ViewPreparedWaiter","Target view is shown on screen, view prepared");
            waiting = false;
            callback.onViewPrepared(viewToWaitingFor);
        } else {
            Log.i("ViewPreparedWaiter","Target view is not shown on screen, add global layout listener");
            final ViewTreeObserver viewTreeObserver = viewToWaitingFor.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void stopWait() {
        Log.i("ViewPreparedWaiter","stop wait");
        if (waiting) {
            waiting = false;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection deprecation
                viewToWaitingFor.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
            } else {
                viewToWaitingFor.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
            }
        }
    }

    public boolean isWaiting() {
        return waiting;
    }

    private boolean isShownOnScreen() {
        Rect waitingViewRect = new Rect();
        boolean waitingViewHasBeenDrawnOnScreen = viewToWaitingFor.getGlobalVisibleRect(waitingViewRect);

        //If WaitingView is not shown
        if (!(waitingViewHasBeenDrawnOnScreen && viewToWaitingFor.isShown())) {
            Log.i("ViewPreparedWaiter","Target view is not shown: waitingViewHasBeenDrawnOnScreen=" + waitingViewHasBeenDrawnOnScreen + " viewToWaitingFor.isShown():" + viewToWaitingFor.isShown());
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (!viewToWaitingFor.isAttachedToWindow()) {
                Log.i("ViewPreparedWaiter","Target view is not shown: viewToWaitingFor.isAttachedToWindow()=" + viewToWaitingFor.isAttachedToWindow());
                return false;
            }
        }

        Log.i("ViewPreparedWaiter","TargetView is shown on screen");
        return true;
    }

    public interface OnViewPrepared {
        void onViewPrepared(View viewToWait);
    }
}