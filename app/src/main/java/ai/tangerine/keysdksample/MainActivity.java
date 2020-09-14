package ai.tangerine.keysdksample;

import ai.tangerine.keysdk.KeyConstants;
import ai.tangerine.keysdk.KeyListener;
import ai.tangerine.keysdk.KeySdk;
import ai.tangerine.keysdk.KeySdkIllegalArgumentException;
import ai.tangerine.keysdk.KeySdkIllegalStateException;
import ai.tangerine.keysdk.model.KeyBookingInfo;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private AppCompatButton btnAddBooking;
    private AppCompatButton btnLock;
    private AppCompatButton btnUnlock;
    private AppCompatButton btnConnect;
    private AppCompatButton btnDisconnect;
    private AppCompatButton btnLogout;
    private AppCompatButton btnBookingInfo;
    private AppCompatButton btnLastBookingInfo;
    private AppCompatTextView txtBookingInfo;
    AppCompatSpinner spinner;
    private static final String TAG = "MainActivity";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final int REQUEST_ENABLE_BT = 1000;
    public static final int REQ_LOGIN = 2000;

    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddBooking = findViewById(R.id.btn_add_booking);
        btnLock = findViewById(R.id.btn_lock);
        btnUnlock = findViewById(R.id.btn_unlock);
        btnConnect = findViewById(R.id.btn_connect);
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnLogout = findViewById(R.id.btn_logout);
        btnBookingInfo = findViewById(R.id.btn_booking_info);
        btnLastBookingInfo = findViewById(R.id.btn_last_booking_info);
        txtBookingInfo = findViewById(R.id.txt_booking_info);
        spinner = findViewById(R.id.spinner);

        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lock();
            }
        });

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlock();
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnect();
            }
        });

        btnBookingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBookingInfo();
            }
        });

        btnLastBookingInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLastBookingInfo();
            }
        });

        btnAddBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, REQ_LOGIN);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                disconnect();
                getBookingInfo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        showLockBtn(false);
        showUnlockBtn(false);
        checkConnected();
        fetchBookings();

    }

    private void getBookingInfo() {
        KeyBookingInfo keyBookingInfo = KeySdk.getBookingInfo(getCurrentSpinnerItem());
        if(keyBookingInfo != null) {
            txtBookingInfo.setText(keyBookingInfo.toString());
            //if(keyBookingInfo.lastLockState == KeyConstants.STATE_LOCKED) {
            //    showLockBtn(false);
            //    showUnlockBtn(true);
            //} else {
            //    showLockBtn(true);
            //    showUnlockBtn(false);
            //}
        } else {
            txtBookingInfo.setText("null");
        }
    }

    private void getLastBookingInfo() {
        KeyBookingInfo keyBookingInfo = KeySdk.getLastBookingInfo();
        if(keyBookingInfo != null) {
            txtBookingInfo.setText(keyBookingInfo.toString());
        } else {
            txtBookingInfo.setText("null");
        }
    }

    private void fetchBookings() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                HashMap<String, KeyBookingInfo> bookings = KeySdk.getAllBookings();
                if (bookings != null && !bookings.isEmpty()) {
                    showAllBtn(true);
                    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                            android.R.layout.simple_spinner_item, android.R.id.text1);
                    spinnerAdapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(spinnerAdapter);
                    for (String key : bookings.keySet()) {
                        spinnerAdapter.add(key);
                    }
                    spinnerAdapter.notifyDataSetChanged();
                    String message =
                            String.format(getString(R.string.bookings_count), bookings.size());
                    txtBookingInfo.setText(message);
                } else {
                    spinner.setAdapter(null);
                    showAllBtn(false);
                    txtBookingInfo.setText(R.string.no_bookings);
                }
            }
        });
    }

    private String getCurrentSpinnerItem() {
        Object selectedItem = spinner.getSelectedItem();
        if(selectedItem != null) {
            return spinner.getSelectedItem().toString();
        }
        return null;
    }

    private void checkConnected() {
        // todo check for already connected to avoid re-connect
        if(KeySdk.isConnected()) {
            showConnectBtn(false);
            showDisconnectBtn(true);
            int state = KeySdk.getLastLockStatus(getCurrentSpinnerItem());
            switch (state) {
                case KeyConstants.STATE_LOCKED:
                    showLockBtn(false);
                    showUnlockBtn(true);
                    break;
                case KeyConstants.STATE_UNLOCKED:
                    showLockBtn(true);
                    showUnlockBtn(false);
                    break;
            }
        } else {
            showConnectBtn(true);
            showDisconnectBtn(false);
        }
    }

    @SuppressLint("MissingPermission")
    private void connect() {
        // todo Step-5
        showProgressBar(true);
        try {
            KeySdk.connect(getCurrentSpinnerItem(), keyListener);
        } catch (KeySdkIllegalStateException e) {
            showProgressBar(false);
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            logout();
        } catch (KeySdkIllegalArgumentException e) {
            showProgressBar(false);
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void lock() {
        // todo Step-6.1
        showProgressBar(true);
        try {
            KeySdk.lock(keyListener);
        } catch (KeySdkIllegalArgumentException e) {
            showProgressBar(false);
            e.printStackTrace();
        } catch (KeySdkIllegalStateException e) {
            showProgressBar(false);
            e.printStackTrace();
            switch(e.getMessage()) {
                case KeyConstants.EXCEPTION_NOT_AUTHENTICATED:
                    logout();
                    break;
                case KeyConstants.EXCEPTION_NOT_CONNECTED:
                    connect();
                    break;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void unlock() {
        // todo Step-6.2
        showProgressBar(true);
        try {
            KeySdk.unlock(keyListener);
        } catch (KeySdkIllegalArgumentException e) {
            showProgressBar(false);
            e.printStackTrace();
        } catch (KeySdkIllegalStateException e) {
            showProgressBar(false);
            e.printStackTrace();
            switch(e.getMessage()) {
                case KeyConstants.EXCEPTION_NOT_AUTHENTICATED:
                    logout();
                    break;
                case KeyConstants.EXCEPTION_NOT_CONNECTED:
                    connect();
                    break;
            }
        }
    }

    private void logout() {
        try {
            KeySdk.logout(getCurrentSpinnerItem(), keyListener);
            fetchBookings();
        } catch (KeySdkIllegalArgumentException | KeySdkIllegalStateException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        // todo Step-7
        try {
            KeySdk.disconnect(keyListener);
        } catch (KeySdkIllegalStateException | KeySdkIllegalArgumentException e) {
            e.printStackTrace();
        }

        showProgressBar(false);
        showConnectBtn(true);
        showDisconnectBtn(false);
        showLockBtn(false);
        showUnlockBtn(false);
    }

    KeyListener keyListener = new KeyListener() {
        @Override
        public void onStateChanged(String bookingId, int state) {
            String message = KeyConstants.getMessage(state);
            Log.i(TAG, "onStateChanged: " + state + ":" + message);
            switch (state) {
                case KeyConstants.STATE_CONNECTED:
                    showProgressBar(false);
                    showConnectBtn(false);
                    showDisconnectBtn(true);
                    break;
                case KeyConstants.STATE_CAR_NOT_FOUND:
                    showProgressBar(false);
                    errorToast(message);
                    break;
                case KeyConstants.STATE_CONNECTING:
                    showProgressBar(true);
                    break;
                case KeyConstants.STATE_DISCONNECTED:
                    showProgressBar(false);
                    errorToast(message);
                    showConnectBtn(true);
                    showDisconnectBtn(false);
                    break;
                case KeyConstants.STATE_BOOKING_VALID:
                    break;
                case KeyConstants.STATE_BOOKING_INVALID:
                    showProgressBar(false);
                    errorToast(message);
                    logout();
                    break;
                case KeyConstants.STATE_LOCKED:
                    showProgressBar(false);
                    errorToast(message);
                    showLockBtn(false);
                    showUnlockBtn(true);
                    showConnectBtn(false);
                    break;
                case KeyConstants.STATE_UNLOCKED:
                    showProgressBar(false);
                    errorToast(message);
                    showLockBtn(true);
                    showUnlockBtn(false);
                    showConnectBtn(false);
                    break;
            }
        }

        @Override
        public void onAccessError(String bookingId, int error) {
            showProgressBar(false);
            String message = KeyConstants.getMessage(error);
            Log.i(TAG, "onAccessError: " + error + ":" + message);
            switch (error) {
                case KeyConstants.ERROR_PERMISSION_DENIED_TO_CONNECT:
                    showProgressBar(false);
                    errorToast(message);
                    logout();
                    break;
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
                    logout();
                    break;
                case KeyConstants.ERROR_BOOKING_EXPIRED:
                    errorToast(R.string.booking_session_expired);
                    logout();
                    break;
                case KeyConstants.ERROR_NO_INTERNET:
                    errorToast(R.string.no_internet);
                    break;
            }
        }

        @Override
        public void onBookingInfo(KeyBookingInfo keyBookingInfo) {
            Log.i(TAG, "Booking Reference:" + keyBookingInfo.getBookingRef());
            Log.i(TAG, "Phone Number:" + keyBookingInfo.getPhoneNum());
            Log.i(TAG, "Vehicle Number:" + keyBookingInfo.getVehicleNum());
            Log.i(TAG, "Last Lock State:" + keyBookingInfo.getLastLockState());
            Log.i(TAG, "Start Time:" + keyBookingInfo.getStartTime());
            Log.i(TAG, "End Time:" + keyBookingInfo.getEndTime());
        }
    };

    private void showLockBtn(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnLock.setEnabled(show);
                btnLock.setClickable(show);
            }
        });
    }

    private void showUnlockBtn(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnUnlock.setEnabled(show);
                btnUnlock.setClickable(show);
            }
        });
    }

    private void showConnectBtn(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnConnect.setEnabled(show);
                btnConnect.setClickable(show);
            }
        });
    }

    private void showDisconnectBtn(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDisconnect.setEnabled(show);
                btnDisconnect.setClickable(show);
            }
        });
    }

    private void showAllBtn(final boolean show) {
        showLockBtn(show);
        showConnectBtn(show);
        showUnlockBtn(show);
        showDisconnectBtn(show);
    }

    private void redirectToLogin() {
        finish();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }
    //-----------------------------------------------------------
    // BT related
    //-----------------------------------------------------------

    private void showBtEnableDialog() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(getApplicationContext(), R.string.bt_enabled, Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else if(requestCode == REQ_LOGIN) {
            switch (resultCode) {
                case RESULT_OK:
                    Toast.makeText(getApplicationContext(), R.string.booking_added, Toast.LENGTH_SHORT).show();
                    fetchBookings();
                    break;
                default:
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), R.string.booking_add_fail, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    //-----------------------------------------------------------
    // Location permission related
    //-----------------------------------------------------------

    private void askForPermission() {
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                    .setTitle(R.string.title_location_permission)
                    .setMessage(R.string.text_location_permission)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .create()
                    .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (MY_PERMISSIONS_REQUEST_LOCATION == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                errorToast(R.string.permission_granted);
            } else {
                errorToast(R.string.permission_not_granted);
                openAppSettings();
            }
        }
    }

    public void openAppSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------------------------
    // Location enable related
    //-----------------------------------------------------------

    private void showLocationEnableDialog() {
        new AlertDialog.Builder(this)
            .setMessage(R.string.dialog_location_enable_title)
            .setPositiveButton(R.string.dialog_location_enable_yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    navigateToLocationSettings();
                    dialogInterface.dismiss();
                }
            })
            .setNegativeButton(R.string.dialog_location_enable_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            })
            .setCancelable(false)
            .show();
    }

    private void navigateToLocationSettings() {
        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(myIntent);
    }

    //-----------------------------------------------------------
    // Other UI
    //-----------------------------------------------------------

    private void errorToast(final @StringRes int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void errorToast(final String res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showProgressBar(boolean show) {
        if (show) {
            if (progressDialog == null) {
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(MainActivity.this);
                View view = getLayoutInflater().inflate(R.layout.progress, null);
                alert.setView(view);
                progressDialog = alert.create();
                if (progressDialog.getWindow() != null) {
                    progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
                }
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing() && !this.isFinishing()) {
                progressDialog.dismiss();
            }
        }
    }

}