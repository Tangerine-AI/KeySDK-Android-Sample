package ai.tangerine.keysdksample;

import ai.tangerine.keysdk.KeySdkIllegalStateException;
import android.app.Application;

import ai.tangerine.keysdk.KeySdk;
import android.util.Log;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // todo step-3
        try {
            KeySdk.init(getApplicationContext());
        } catch (KeySdkIllegalStateException e) {
            e.printStackTrace();
        }
    }
}
