package tw.firemaples.onscreenocr.translate;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import tw.firemaples.onscreenocr.R;
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView;
import tw.firemaples.onscreenocr.utils.Tool;

public class GoogleTranslateUtil {
    private static final String PACKAGE_NAME_GOOGLE_TRANSLATE = "com.google.android.apps.translate";

    public static boolean isGoogleTranslateInstalled(Context context) {
        return Tool.isPackageInstalled(context, PACKAGE_NAME_GOOGLE_TRANSLATE);
    }

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

    public static void showGoogleTranslateNotInstallDialog(final Context context) {
        DialogView dialogView = new DialogView(context);
        dialogView.reset();
        dialogView.setType(DialogView.Type.CONFIRM_CANCEL);
        dialogView.setTitle("Google Translate app not found");
        dialogView.setContentMsg("To use Lite Mode, please install Google Translate app.");
        dialogView.getOkBtn().setText("Install");
        dialogView.setCallback(new DialogView.OnDialogViewCallback() {
            @Override
            public void OnConfirmClick(DialogView dialogView) {
                super.OnConfirmClick(dialogView);
                Tool.openPlayStore(context, PACKAGE_NAME_GOOGLE_TRANSLATE);
            }
        });
        dialogView.attachToWindow();
    }
}
