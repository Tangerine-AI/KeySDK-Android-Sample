package ai.tangerine.keysdksample;

import android.app.Application;

import ai.tangerine.keysdk.KeySdk;

public class SampleApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // todo step-3
        KeySdk.init(getApplicationContext());
    }
}
