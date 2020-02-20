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
   implementation 'ai.tangerine:keysdk:1.0.0-beta01'
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
       KeySdk.init(getApplicationContext());
   }
}
```
#### Step 4

Validate your booking reference using the following method:

```
KeySdk.validateBooking(bookingRef, phoneNum, keyListener);
```

#### Step 5

Connect to the device once validation is successful using the following method.

```
KeySdk.connect(keyListener);
```

#### Step 6

Lock and unlock the vehicle using the following APIs. You need to be connected before using these 2 methods. If your phone is not connected then it will throw IllegalArgumentException (“Please connect to the device before executing the command”)
Use step 5 to connect before using the lock and unlock API.
```

try {
   KeySdk.lock(keyListener);
} catch(Exception e) {
   e.printStackTrace();
   KeySdk.connect(keyListener);
}
```

```

try {
   KeySdk.unlock(keyListener);
} catch(Exception e) {
   e.printStackTrace();
   KeySdk.connect(keyListener);
}
```
#### Step 7

Disconnect the device using the following API.

```
KeySdk.disconnect(keyListener);
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

