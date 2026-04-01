package com.example.CafeteriaApp.Helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.CafeteriaApp.R;

/**
 * Helper class to handle application notifications safely.
 */
public class NotificationHelper {

    public static final String CHANNEL_ORDERS_ID = "orders_status";
    public static final String CHANNEL_UPDATES_ID = "app_updates";

    private static void sendNotification(Context context, String channelId, String channelName,
                                        String title, String text, int notificationId) {

        // 1. Create Channel (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // 3. Safe Notify
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        
        notificationManager.notify(notificationId, builder.build());
    }

    public static void showOrderStatus(Context context, String text, int orderId) {
        String channelName = context.getString(R.string.notif_channel_orders_name);
        String title = context.getString(R.string.notif_order_update_title);
        sendNotification(context, CHANNEL_ORDERS_ID, channelName, title, text, orderId);
    }

    public static void showUpdateNotification(Context context, String title, String text) {
        String channelName = context.getString(R.string.notif_channel_updates_name);
        sendNotification(context, CHANNEL_UPDATES_ID, channelName, title, text, 999);
    }
}
