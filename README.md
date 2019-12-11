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
   implementation 'ai.tangerine:keysdk:1.0.0-alpha8'
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