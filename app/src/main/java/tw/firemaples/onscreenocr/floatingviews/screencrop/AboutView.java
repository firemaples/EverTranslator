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
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.FloatingView;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 08/12/2016.
 */

public class AboutView extends FloatingView {
    private int[] linkResArray = new int[]{R.id.tv_link1, R.id.tv_link2, R.id.tv_link3, R.id.tv_link4, R.id.tv_link5, R.id.tv_link6};

    public AboutView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_about;
    }

    @Override
    protected int getLayoutSize() {
        return WindowManager.LayoutParams.MATCH_PARENT;
    }

    private void setViews(View rootView) {
        rootView.findViewById(R.id.bt_dialogClose).setOnClickListener(onClickListener);
        Arrays.sort(linkResArray);

        for (int linkRes : linkResArray) {
            setupLinkText(linkRes);
        }
    }

    private void setupLinkText(int textViewRes) {
        TextView textView = (TextView) getRootView().findViewById(textViewRes);
        String url = textView.getText().toString();
        textView.setTag(url);
        textView.setText(Html.fromHtml(String.format(Locale.getDefault(), "<u>%s</u>", url)));
        textView.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.bt_dialogClose) {
                AboutView.this.detachFromWindow();
            } else if (Arrays.binarySearch(linkResArray, id) >= 0) {
                String url = (String) v.getTag();
                Tool.getInstance().openBrowser(url);
                AboutView.this.detachFromWindow();
            }
        }
    };
}
