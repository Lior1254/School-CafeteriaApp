package com.example.CafeteriaApp.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkListener listener;

    public interface NetworkListener {
        void onNetworkChanged(boolean isConnected);
    }

    public NetworkChangeReceiver(NetworkListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = (cm != null) ? cm.getActiveNetworkInfo() : null;
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();

        if (listener != null) {
            listener.onNetworkChanged(isConnected);
        }
    }
}