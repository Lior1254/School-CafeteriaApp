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
 * Global helper class for managing and displaying system notifications.
 * Handles channel creation for Android Oreo and above, and permission checks for Android 13+.
 */
public class NotificationHelper {

    public static final String CHANNEL_ORDERS_ID = "orders_status";
    public static final String CHANNEL_UPDATES_ID = "app_updates";

    /**
     * Internal helper to build and send a system notification.
     * Checks for required permissions and creates notification channels if necessary.
     *
     * @param context        The application context.
     * @param channelId      The target notification channel ID.
     * @param channelName    The user-visible name of the channel.
     * @param title          The title displayed in the notification.
     * @param message        The body text of the notification.
     * @param notificationId A unique integer ID for this notification instance.
     */
    private static void sendNotification(Context context, String channelId, String channelName,
                                        String title, String message, int notificationId) {

        // Ensure the notification channel exists for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }
        }

        // Construct the notification using the Builder pattern
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Perform permission check for Android 13 (Tiramisu) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Displays a notification related to order status updates.
     *
     * @param context The application context.
     * @param message The Hebrew text describing the status change.
     * @param orderId The ID used to uniquely identify the notification.
     */
    public static void showOrderStatus(Context context, String message, int orderId) {
        String channelName = context.getString(R.string.notif_channel_orders_name);
        String title = context.getString(R.string.notif_order_update_title);
        sendNotification(context, CHANNEL_ORDERS_ID, channelName, title, message, orderId);
    }

    /**
     * Displays a general application update notification.
     *
     * @param context The application context.
     * @param title   The title of the update notification.
     * @param message The body text of the update notification.
     */
    public static void showGeneralUpdateNotification(Context context, String title, String message) {
        String channelName = context.getString(R.string.notif_channel_updates_name);
        sendNotification(context, CHANNEL_UPDATES_ID, channelName, title, message, 999);
    }
}
