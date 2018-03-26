package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

/**
 * Created by firemaples on 08/12/2016.
 */

public class AboutView extends InfoDialogView {
    private static final int[] linkResArray = new int[]{R.id.tv_link1, R.id.tv_link2, R.id.tv_link3, R.id.tv_link4, R.id.tv_link5, R.id.tv_link6};

    public AboutView(Context context) {
        super(context);
    }

    @Override
    int getContentLayoutId() {
        return R.layout.content_about;
    }

    @Override
    String getTitle() {
        return getContext().getString(R.string.title_about);
    }

    @Override
    protected void setViews(View rootView) {
        super.setViews(rootView);

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
            if (Arrays.binarySearch(linkResArray, id) >= 0) {
                String url = (String) v.getTag();
                Tool.getInstance().openBrowser(url);
                AboutView.this.detachFromWindow();
            }
        }
    };
}
