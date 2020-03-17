# KeySDK-Android-Sample

This guide gives information for the Tangerine-Keysdk integration with the third party android application.

Tangerine-Keysdk has been integrated into this project. You can go to the **TODO** section of the android studio project to check the Library usage steps to integrate it in your application.

Please refer to the KeyConstants class for error and state-related information.

## Library Usage

#### Step 1

To use it in your project, you need to declare the maven url inside the project level build.gradle.

```
allprojects {
   repositories {      
       maven {
           url "http://jfrog.tangerine.ai/artifactory/sdk-keyless"
           credentials {
               username = "carro"
               password = "AP52Hs1TdA7WuEr8abctKEmSujP"
           }
       }
   }
}
```


#### Step 2

Open `build.gradle` inside module that you want to use the library and simply add a dependency.

```
dependencies {
   implementation 'ai.tangerine:keysdk:1.0.0-beta05'
}
```

#### Step 3

Open `Application` class file inside your app or create the Application class file and add the following code in `onCreate()` method.

```
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
```
#### Step 4

Validate your booking reference using the following method:

```
        try {
            KeySdk.validateBooking(bookingRef, phoneNum, new KeyListener() {
                @Override
                public void onStateChanged(int i) {
    
                }
    
                @Override
                public void onAccessError(int i) {
                    showProgressBar(false);
                    switch (i) {
                        case KeyConstants.ERROR_BT_NOT_ENABLED:
                            showBtEnableDialog();
                            break;
                        case KeyConstants.ERROR_LOCATION_NOT_ENABLED:
                            showLocationEnableDialog();
                            break;
                        case KeyConstants.ERROR_LOCATION_PERMISSION_NOT_GRATED:
                            askForPermission();
                            break;
                        case KeyConstants.ERROR_BOOKING_WILL_START:
                            errorToast(R.string.booking_time_not_started);
                            break;
                        case KeyConstants.ERROR_BOOKING_VALIDATION_FAILED:
                            errorToast(R.string.invalid_booking_info);
                            break;
                        case KeyConstants.ERROR_BOOKING_EXPIRED:
                            errorToast(R.string.booking_session_expired);
                            break;
                        case KeyConstants.ERROR_NO_INTERNET:
                            errorToast(R.string.no_internet);
                            break;
                    }
                }
    
                @Override
                public void onBookingInfo(String s, long l, long l1) {
                    showProgressBar(false);
                    Log.i(TAG, "onBookingInfo:" + s);
                    Log.i(TAG, "start time:" + l);
                    Log.i(TAG, "end time:" + l1);
                    // validation successful go for connection 
                    connect();
                }
            });
        } catch (KeySdkIllegalStateException | KeySdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
```

#### Step 5

Connect to the device once validation is successful using the following method.

```
        try {
            KeySdk.connect(keyListener);
        } catch (KeySdkIllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            redirectToLogin();
        } catch (KeySdkIllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
```

#### Step 6

Lock and unlock the vehicle using the following APIs. You need to be connected before using these 2 methods. If your phone is not connected then it will throw IllegalArgumentException (“Please connect to the device before executing the command”)
Use step 5 to connect before using the lock and unlock API.
```

        try {
            KeySdk.lock(keyListener);
        } catch (KeySdkIllegalArgumentException e) {
            e.printStackTrace();
        } catch (KeySdkIllegalStateException e) {
            e.printStackTrace();
            switch(e.getMessage()) {
                case KeyConstants.EXCEPTION_NOT_AUTHENTICATED:
                    redirectToLogin();
                    break;
                case KeyConstants.EXCEPTION_NOT_CONNECTED:
                    connect();
                    break;
            }
        }
```

```

        try {
            KeySdk.unlock(keyListener);
        } catch (KeySdkIllegalArgumentException e) {
            e.printStackTrace();
        } catch (KeySdkIllegalStateException e) {
            e.printStackTrace();
            switch(e.getMessage()) {
                case KeyConstants.EXCEPTION_NOT_AUTHENTICATED:
                    redirectToLogin();
                    break;
                case KeyConstants.EXCEPTION_NOT_CONNECTED:
                    connect();
                    break;
            }
        }
```
#### Step 7

Disconnect the device using the following API.

```
        try {
            KeySdk.disconnect(keyListener);
        } catch (KeySdkIllegalStateException | KeySdkIllegalArgumentException e) {
            e.printStackTrace();
        }
```

#### Step 8

logout by clearing existing booking information to start fresh.

```
        try {
            KeySdk.logout(keyListener);
        } catch (KeySdkIllegalStateException | KeySdkIllegalArgumentException e) {
            e.printStackTrace();
        }
```

#### Get BT connection state

you can check if device is already connected with the Jido Sense device by using below api

```
        
            boolean state = KeySdk.isConnected();
        
```

#### Get last saved state

you can get the last saved state of car by calling this api. If not state is saved in sdk then it will return ``` KeyConstants.STATE_LOCKED ```

```
        
            int state = KeySdk.getLastLockStatus();
        
```

#### Validation Step
 
Please find sample api as below for checking if booking is been validated or not. 
Sample app decides whether to go to main screen or login screen based on booking validation.
User don't have to validate the booking information again and again until it expires or modified.
This will return true once ``` KeySdk.validateBooking(); ``` is successful.
```
    KeySdk.isValidationDone();
```

```
        if (KeySdk.isValidationDone()) {
            // go for connection
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        } else {
             // go for login
             Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
             startActivity(intent);
             finish();   
        }
```


#### Error handling

There are several error scenarios come when booking validation happens or while executing the command. User has to pass ``` KeyListener ``` object to handle error case.

Please find below list of errors that can occur during the booking validation.

1. ``` KeyConstants.ERROR_BT_NOT_ENABLED ```  :  This error occurs in case when user has not enabled the bluetooth.
2. ``` KeyConstants.ERROR_BT_NOT_AVAILABLE ```  :  This error occurs in case when device does not support bluetooth.
3. ``` KeyConstants.ERROR_LOCATION_NOT_ENABLED ``` : This error occurs in case when user has not enabled the Location.
4. ``` KeyConstants.ERROR_LOCATION_PERMISSION_NOT_GRATED ``` : This error occurs in case when user has not granted Location permission.
5. ``` KeyConstants.ERROR_NO_INTERNET ``` : This error occurs while validation of booking (``` validateBooking() ``` method) and internet is not there.
6. ``` KeyConstants.ERROR_BOOKING_WILL_START ``` : This error occurs when booking information is valid but booking has not yet started.
7. ``` KeyConstants.ERROR_BOOKING_EXPIRED ``` : This error occurs when booking is expired.
6. ``` KeyConstants.ERROR_BOOKING_VALIDATION_FAILED ``` : This error occurs when booking information is invalid or there is timeout for validation request.
9. ``` KeyConstants.ERROR_BOOKING_NOT_VALIDATED ``` : This error occurs when app tries to use connect / lock / unlock feature without validating the booking information. 
10. ``` KeyConstants.ERROR_PERMISSION_DENIED_TO_CONNECT ``` : This error occurs when another app is already connected to vehicle/device.


#### State information while connecting to the device or executing the lock/unlock feature

Please find below list of state that can occur during the connection and execution of lock/unlock features.

1. ``` KeyConstants.STATE_CONNECTING ```  :  App starts connecting to the device.
2. ``` KeyConstants.STATE_CONNECTED ```  :  App is connected to device.
3. ``` KeyConstants.STATE_DISCONNECTED ``` : App is disconnected to device because of range.
4. ``` KeyConstants.STATE_BOOKING_INVALID ``` : App goes to this state when already validated booking is expired or device invalidates the booking information when try to execute the lock/unlock feature.
5. ``` KeyConstants.STATE_BOOKING_VALID ``` : App goes to this state when booking is valid while executing the ``` validateBooking() ```
6. ``` KeyConstants.STATE_CAR_NOT_FOUND ``` : When app is unable to connect to the device after ~25 seconds of time or app is not near to the device.
7. ``` KeyConstants.ERROR_BOOKING_EXPIRED ``` : This error occurs when booking is expired.
6. ``` KeyConstants.STATE_LOCKED ``` : When device is locked
9. ``` KeyConstants.STATE_UNLOCKED ``` : When device is unlocked 


#### Exception information while connecting to the device or executing the lock/unlock feature

1. ``` KeyConstants.EXCEPTION_SDK_NOT_INIT ```  :  When SDK is not initialised.
2. ``` KeyConstants.EXCEPTION_KEY_LISTENER ```  :  When ``` KeyListener ``` is null.
3. ``` KeyConstants.EXCEPTION_CONTEXT ```  :  When ``` Context ``` is null.
4. ``` KeyConstants.EXCEPTION_BOOKING_ID ```  :  When ``` bookingId ``` passed is null or empty while ``` validateBooking() ```.
5. ``` KeyConstants.EXCEPTION_PHONE_NUM ```  :  When ``` phoneNum ``` passed is null or empty while ``` validateBooking() ```.
5. ``` KeyConstants.EXCEPTION_NOT_AUTHENTICATED ```  :  When booking is not validated and try to execute ``` connect() ``` , ``` lock() ``` or ``` unlock() ```
5. ``` KeyConstants.EXCEPTION_NOT_CONNECTED ```  :  When app is not connected to device and try to execute the ``` lock() ``` or ``` unlock() ```.

#### Note
1. We recommend to validate the booking for the first time and then save the booking information for further usage rather than validating every time you connect to the device.
2. The sequence of usage is to ```validateBooking()``` -> save booking -> ```connect()``` -> ```lock()``` / ```unlock()```. Once the booking is validated you can skip ```validateBooking()``` until you get invalid booking error.
3. ```lock()``` / ```unlock()``` this can be used only after ```connect()``` gets successful through ```KeyConstants.STATE_CONNECTED```.

For more detailed implementation please refer the sample app code.

