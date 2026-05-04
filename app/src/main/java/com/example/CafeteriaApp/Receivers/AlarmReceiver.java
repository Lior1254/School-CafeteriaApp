package com.example.CafeteriaApp.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.CafeteriaApp.Helpers.NotificationHelper;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String OrderStatus = "OrderStatus";
    public static final String AppUpdate = "AppUpdate";
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getStringExtra("Type").equals(OrderStatus))
        {
            String msg = intent.getStringExtra("text");
            int orderId = intent.getIntExtra("orderID",-999);

            NotificationHelper.showOrderStatus(context,msg,orderId);
        }
        else if(intent.getStringExtra("Type").equals(AppUpdate))
        {
            //For Future
        }
    }
}