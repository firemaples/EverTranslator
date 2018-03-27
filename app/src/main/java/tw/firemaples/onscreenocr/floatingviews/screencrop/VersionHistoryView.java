package tw.firemaples.onscreenocr.floatingviews.screencrop;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tw.firemaples.onscreenocr.R;

/**
 * Created by louis1chen on 26/03/2018.
 */

public class VersionHistoryView extends InfoDialogView {
    private static final String KEY_VERSION_CODE = "KEY_VERSION_CODE";
    private static final String KEY_VERSION_MESSAGE = "KEY_VERSION_MESSAGE";

    private SimpleAdapter adapter;
    private List<HashMap<String, String>> dataList = new ArrayList<>();

    public VersionHistoryView(Context context) {
        super(context);
    }

    @Override
    int getButtonMode() {
        return MODE_CLOSE;
    }

    @Override
    int getContentLayoutId() {
        return R.layout.content_version_history;
    }

    @Override
    String getTitle() {
        return getContext().getString(R.string.title_version_history);
    }

    @Override
    protected void setViews(View rootView) {
        super.setViews(rootView);

        ListView lv_versionHistory = (ListView) rootView.findViewById(R.id.lv_versionHistory);

        adapter = new SimpleAdapter(getContext(), dataList, R.layout.item_version_history, new String[]{KEY_VERSION_CODE, KEY_VERSION_MESSAGE}, new int[]{R.id.tv_versionCode, R.id.tv_versionMessage});
        lv_versionHistory.setAdapter(adapter);

        Resources resources = getContext().getResources();
        String[] versionCodes = resources.getStringArray(R.array.versionCodes);
        String[] versionMessages = resources.getStringArray(R.array.versionMessage);

        for (int i = 0; i < versionCodes.length; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put(KEY_VERSION_CODE, versionCodes[i]);
            map.put(KEY_VERSION_MESSAGE, versionMessages[i]);

            dataList.add(map);
        }

        adapter.notifyDataSetChanged();
    }
}
