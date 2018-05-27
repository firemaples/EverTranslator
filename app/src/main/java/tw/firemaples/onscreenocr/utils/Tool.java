package tw.firemaples.onscreenocr.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.widget.Toast;

import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

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

    public void showMsg(String msg) {
        if (getContext() == null) {
            return;
        }

        new StyleableToast.Builder(context)
                .text(msg)
                .textColor(Color.WHITE)
                .backgroundColor(Color.BLACK)
                .length(Toast.LENGTH_LONG)
                .show();
    }

    public void showErrorMsg(String msg) {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();

        new StyleableToast.Builder(context)
                .text(msg)
                .textColor(Color.WHITE)
                .backgroundColor(Color.RED)
                .length(Toast.LENGTH_LONG)
                .show();
    }

    public static String replaceAllLineBreaks(String str, String replaceWith) {
        if (str == null) {
            return null;
        }
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

    public static void openPlayStore(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4) {
                                return sAddr;
                            }
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
