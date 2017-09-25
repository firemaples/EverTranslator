package tw.firemaples.onscreenocr.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by firemaples on 07/07/2017.
 */

public class SignatureUtil {
    private static final String SIGNATURE_SHA = "gK7G8ZtYKeXYMsWzOZ0wP+rTP50=\n";
    private static final int SIGNATURE_HASH = 116585913;

    public static boolean validateSignature(Context context) throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {
        String currentSignatureSHA = getCurrentSignatureSHA(context);
        if (currentSignatureSHA == null) {
            return false;
        } else {
            return currentSignatureSHA.replaceAll("\n", "").equals(SIGNATURE_SHA.replaceAll("\n", ""));
        }
    }

    public static String getCurrentSignatureSHA(Context context) throws NoSuchAlgorithmException, PackageManager.NameNotFoundException {
        Signature signature = getCurrentSignature(context);
        if (signature == null) {
            return null;
        }
        byte[] signatureBytes = signature.toByteArray();
        MessageDigest md = MessageDigest.getInstance("SHA");
        md.update(signatureBytes);
        return Base64.encodeToString(md.digest(), Base64.DEFAULT);
    }

    public static int getCurrentSignatureHashCode(Context context) throws PackageManager.NameNotFoundException {
        Signature signature = getCurrentSignature(context);
        if (signature == null) {
            return -1;
        }
        return signature.hashCode();
    }

    public static Signature getCurrentSignature(Context context) throws PackageManager.NameNotFoundException {
        Signature[] signatures = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
        if (signatures != null && signatures.length > 0) {
            return signatures[0];
        }
        return null;
    }
}
