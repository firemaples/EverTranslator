package tw.firemaples.onscreenocr.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * From http://stackoverflow.com/a/31340960/2906153
 */
public class HomeWatcher {

    private Context mContext;
    private IntentFilter mFilter;
    private OnHomePressedListener mListener;
    private InnerReceiver mReceiver;

    private boolean isWatching;

    public HomeWatcher(Context context) {
        mContext = context;
        mFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    public void setOnHomePressedListener(OnHomePressedListener listener) {
        mListener = listener;
        if (mReceiver == null) {
            mReceiver = new InnerReceiver();
        }
    }

    public void startWatch() {
        if (mReceiver != null) {
            mContext.registerReceiver(mReceiver, mFilter);
            isWatching = true;
        }
    }

    public void stopWatch() {
        if (mReceiver != null && isWatching) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    @SuppressWarnings({"unused", "SpellCheckingInspection"})
    class InnerReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    Tool.logError("HomeWatcher, action:" + action + ",reason:" + reason);
                    if (mListener != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            mListener.onHomePressed();
                        } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                            mListener.onHomeLongPressed();
                        }
                    }
                }
            }
        }
    }

    public interface OnHomePressedListener {
        void onHomePressed();

        void onHomeLongPressed();
    }
}
