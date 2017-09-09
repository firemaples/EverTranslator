package tw.firemaples.onscreenocr;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;
import tw.firemaples.onscreenocr.utils.SignatureUtil;

/**
 * Created by louis1chen on 11/08/2017.
 */

public class CoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (!Fabric.isInitialized()) {
            Fabric.with(this, new Crashlytics());
        }

        FirebaseApp.initializeApp(this);

        validateSignature();
    }

    protected void validateSignature() {
        try {
            String sha = SignatureUtil.getCurrentSignatureSHA(this);
            int hashCode = SignatureUtil.getCurrentSignatureHashCode(this);
            boolean result = SignatureUtil.validateSignature(this);
            Crashlytics.setString("Signature_SHA", sha);
            Crashlytics.setInt("Signature_hashCode", hashCode);
            Crashlytics.setBool("Signature_SHA_is_correct", result);
            Crashlytics.setString("Package_name", getPackageName());
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.setString("ValidateSignatureFailed", e.getMessage());
            Crashlytics.log(Log.getStackTraceString(e));
        }
    }
}
