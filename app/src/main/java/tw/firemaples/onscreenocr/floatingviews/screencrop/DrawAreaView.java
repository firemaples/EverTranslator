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

package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.views.AreaSelectionView;

/**
 * Created by firemaples on 21/10/2016.
 */

public class DrawAreaView extends FloatingView {
    private AreaSelectionView view_areaSelectionView;

    public DrawAreaView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_draw_area;
    }

    @Override
    protected boolean fullScreenMode() {
        return true;
    }

    protected void setViews(View rootView) {
        view_areaSelectionView =
                (AreaSelectionView) rootView.findViewById(R.id.view_areaSelectionView);
        view_areaSelectionView.setMaxRectCount(1);
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    public AreaSelectionView getAreaSelectionView() {
        return view_areaSelectionView;
    }

    @Override
    public void detachFromWindow() {
        view_areaSelectionView.clear();
        super.detachFromWindow();
    }
}
