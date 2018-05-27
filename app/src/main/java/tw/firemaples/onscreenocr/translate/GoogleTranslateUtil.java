package tw.firemaples.onscreenocr.translate;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.utils.Tool;

public class GoogleTranslateUtil {
    public static boolean start(Context context, String lang, String text) {
        Intent intent = new Intent();
        intent.setType("text/plain");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent.setAction(Intent.ACTION_PROCESS_TEXT);
            intent.putExtra(Intent.EXTRA_PROCESS_TEXT, text);
            intent.putExtra("key_language_to", lang);
            intent.setPackage("com.google.android.apps.translate");
        } else {
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.putExtra("key_text_input", text);
            intent.putExtra("key_text_output", "");
            intent.putExtra("key_language_from", "auto");
            intent.putExtra("key_language_to", lang);
            intent.putExtra("key_suggest_translation", "");
            intent.putExtra("key_from_floating_window", false);
            intent.setComponent(new ComponentName(
                    "com.google.android.apps.translate",
                    //Change is here
                    //"com.google.android.apps.translate.HomeActivity"));
                    "com.google.android.apps.translate.TranslateActivity"));
        }

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Tool.getInstance().showErrorMsg(context.getString(R.string.error_googleTranslatorNotInstalled));
            return false;
        }
    }
}
