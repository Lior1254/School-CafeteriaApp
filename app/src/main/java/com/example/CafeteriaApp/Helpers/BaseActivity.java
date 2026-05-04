package com.example.CafeteriaApp.Helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;

import com.example.CafeteriaApp.Models.Order;
import com.example.CafeteriaApp.Receivers.NetworkChangeReceiver;

/**
 * Base activity class that provides common functionality for all activities in the app,
 * such as network monitoring and status text formatting.
 * Implements NetworkListener to react to real-time connectivity changes.
 */
public abstract class BaseActivity extends AppCompatActivity implements NetworkChangeReceiver.NetworkListener {

    private NetworkChangeReceiver networkReceiver;
    private AlertDialog networkDialog;

    @Override
    protected void onStart() {
        super.onStart();
        // Register the receiver to listen for connectivity changes when the activity is visible
        networkReceiver = new NetworkChangeReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister to prevent memory leaks and crashes when the activity is in the background
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
        // Dismiss the dialog if the activity is stopping to avoid WindowLeaked exceptions
        if (networkDialog != null && networkDialog.isShowing()) {
            networkDialog.dismiss();
        }
    }

    /**
     * Callback method triggered when the network status changes.
     * Automatically shows or hides the connectivity warning dialog.
     * 
     * @param isConnected True if internet is available, false otherwise.
     */
    @Override
    public void onNetworkChanged(boolean isConnected) {
        if (!isConnected) {
            showNoInternetDialog();
        } else {
            // Automatically dismiss the dialog if connection is restored
            if (networkDialog != null && networkDialog.isShowing()) {
                networkDialog.dismiss();
            }
        }
    }

    /**
     * Displays a non-cancelable alert dialog informing the user about the lack of internet connection.
     * Ensures only one dialog is visible at a time.
     */
    private void showNoInternetDialog() {
        // Prevent showing multiple dialogs if one is already visible
        if (networkDialog != null && networkDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("שגיאת חיבור");
        adb.setMessage("שים לב, האינטרנט נותק. חלק מהפעולות באפליקציה לא יעבדו כראוי.");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setCancelable(false); // Force user to acknowledge
        adb.setPositiveButton("הבנתי", (dialog, which) -> dialog.dismiss());
        
        networkDialog = adb.create();
        networkDialog.show();
    }

    /**
     * Checks if the network is currently available (Passive check).
     * @return true if connected, false otherwise.
     */
    public boolean checkNetworkAndShowDialog() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Executes a Firebase operation only if the network is available.
     * If disconnected, shows the warning dialog instead.
     * 
     * @param operation The runnable task to execute.
     */
    protected void executeFirebaseOperation(Runnable operation) {
        if (checkNetworkAndShowDialog()) {
            operation.run();
        } else {
            showNoInternetDialog();
        }
    }

    /**
     * Converts a status code to a user-friendly Hebrew string.
     * Static so it can be used in Adapters and other helpers.
     * 
     * @param status The status constant from Order model.
     * @return A Hebrew string representing the status.
     */
    public static String getStatusText(String status) {
        if (status == null) return "לא ידוע";
        switch (status) {
            case Order.STATUS_PENDING: return "ממתין";
            case Order.STATUS_PREPARING: return "בהכנה";
            case Order.STATUS_READY: return "מוכן";
            case Order.STATUS_COLLECTED: return "נאסף";
            default: return "לא ידוע";
        }
    }
}
