package tw.firemaples.onscreenocr;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;

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
    }
}
