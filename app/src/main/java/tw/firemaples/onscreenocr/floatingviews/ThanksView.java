package tw.firemaples.onscreenocr.floatingviews;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 08/12/2016.
 */

public class ThanksView extends FloatingView {
    private int[] linkResArray = new int[]{R.id.tv_link1, R.id.tv_link2, R.id.tv_link3, R.id.tv_link4, R.id.tv_link5};

    public ThanksView(Context context) {
        super(context);
        setViews(getRootView());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.view_thanks;
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
                ThanksView.this.detachFromWindow();
            } else if (Arrays.binarySearch(linkResArray, id) >= 0) {
                String url = (String) v.getTag();
                Tool.getInstance().openBrowser(url);
                ThanksView.this.detachFromWindow();
            }
        }
    };
}
