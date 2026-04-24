package com.example.CafeteriaApp.Helpers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
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

    /**
     * Internal helper to build and send a system notification.
     * 
     * @param context Application context
     * @param channelId Target notification channel ID
     * @param channelName User-visible name of the channel
     * @param title Notification title
     * @param text Notification message body
     * @param notificationId Unique ID for the notification
     */
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

        // 2. Build Notification with App Logo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_logo) // Small icon in status bar
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo)) // Large icon in notification drawer
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)) // Allows multiline text
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

    /**
     * Shows a notification related to order status changes.
     */
    public static void showOrderStatus(Context context, String text, int orderId) {
        String channelName = context.getString(R.string.notif_channel_orders_name);
        String title = context.getString(R.string.notif_order_update_title);
        sendNotification(context, CHANNEL_ORDERS_ID, channelName, title, text, orderId);
    }

    /**
     * Shows a general app update notification.
     */
    public static void showUpdateNotification(Context context, String title, String text) {
        String channelName = context.getString(R.string.notif_channel_updates_name);
        sendNotification(context, CHANNEL_UPDATES_ID, channelName, title, text, 999);
    }
}
