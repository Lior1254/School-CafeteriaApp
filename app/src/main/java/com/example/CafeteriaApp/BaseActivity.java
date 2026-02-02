package com.example.CafeteriaApp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Checks for internet connection and shows a dialog if disconnected.
     * @return true if connected, false otherwise.
     */
    protected boolean checkNetworkAndShowDialog() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (!isConnected) {
            showNoInternetDialog();
        }

        return isConnected;
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("שגיאת חיבור");
        adb.setMessage("פעולה זו דורשת חיבור לאינטרנט. אנא בדוק את ההגדרות שלך.");
        adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        adb.create().show();
    }
}