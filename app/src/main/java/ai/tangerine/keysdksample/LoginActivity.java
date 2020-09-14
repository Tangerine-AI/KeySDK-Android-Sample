package ai.tangerine.keysdksample;

import ai.tangerine.keysdk.KeySdkIllegalArgumentException;
import ai.tangerine.keysdk.KeySdkIllegalStateException;
import ai.tangerine.keysdk.model.KeyBookingInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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
import android.widget.Toast;

import ai.tangerine.keysdk.KeyConstants;
import ai.tangerine.keysdk.KeyListener;
import ai.tangerine.keysdk.KeySdk;

public class LoginActivity extends AppCompatActivity {

    private AppCompatEditText edtBookingRef;
    private AppCompatEditText edtPhoneNum;
    private AppCompatButton btnLogin;
    private static final String TAG = "LoginActivity";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private BluetoothAdapter mBluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1000;

    private Dialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtBookingRef = findViewById(R.id.booking_ref);
        edtPhoneNum = findViewById(R.id.phone_num);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar(true);
                login(edtBookingRef.getText().toString(), edtPhoneNum.getText().toString());
            }
        });
    }


    private void login(String bookingRef, String phoneNum) {
        // todo step-4
        //validates the booking information. Requires location permission to execute this method.
        // To connect to bluetooth and discover the nearby bluetooth devices location permission is required.
        try {
            KeySdk.validateBooking(bookingRef, phoneNum, new KeyListener() {
                @Override
                public void onStateChanged(String bookingId, int i) {

                }

                @Override
                public void onAccessError(String bookingId, int i) {
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
                public void onBookingInfo(KeyBookingInfo keyBookingInfo) {
                    showProgressBar(false);
                    Log.i(TAG, "onBookingInfo:" + keyBookingInfo.toString());
                    // validation successful go for connection
                    connect();
                }
            });
        } catch (KeySdkIllegalStateException | KeySdkIllegalArgumentException e) {
            e.printStackTrace();
            showProgressBar(false);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void errorToast(final @StringRes int res) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
            }
        });
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
                            ActivityCompat.requestPermissions(LoginActivity.this,
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

    private void connect() {
        setResult(RESULT_OK);
        finish();
    }


    public void showProgressBar(boolean show) {
        if (show) {
            if (progressDialog == null) {
                android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(LoginActivity.this);
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
