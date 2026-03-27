package com.example.CafeteriaApp.Helpers;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import com.example.CafeteriaApp.R;

/**
 * Helper class to handle application notifications.
 * Provides methods to show different types of notifications with specific channels.
 */
public class NotificationHelper {

    // Notification Channel IDs
    public static final String CHANNEL_ORDERS_ID = "orders_status";
    public static final String CHANNEL_UPDATES_ID = "app_updates";

    /**
     * Core method to build and display a notification.
     *
     * @param context        The application context.
     * @param channelId      The unique ID for the notification channel.
     * @param channelName    The user-visible name of the channel.
     * @param title          The title of the notification.
     * @param text           The body text of the notification.
     * @param notificationId The unique ID for this specific notification instance.
     */
    private static void sendNotification(Context context, String channelId, String channelName,
                                        String title, String text, int notificationId) {

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        // Create the notification channel (Required for API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Consider using a custom app icon here
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Display the notification
        notificationManager.notify(notificationId, builder.build());
    }

    /**
     * Displays a notification related to an order's status.
     *
     * @param context The application context.
     * @param text    The message to display (e.g., "Your order is ready!").
     * @param orderId The ID of the order, used as the notification ID to group updates.
     */
    public static void showOrderStatus(Context context, String text, int orderId) {
        String channelName = context.getString(R.string.notif_channel_orders_name);
        String title = context.getString(R.string.notif_order_update_title);
        
        sendNotification(context, CHANNEL_ORDERS_ID, channelName, title, text, orderId);
    }

    /**
     * Displays a general update or promotion notification.
     *
     * @param context The application context.
     * @param title   The notification title.
     * @param text    The notification message.
     */
    public static void showUpdateNotification(Context context, String title, String text) {
        String channelName = context.getString(R.string.notif_channel_updates_name);
        
        // Use a fixed ID (e.g., 999) so new updates replace old ones
        sendNotification(context, CHANNEL_UPDATES_ID, channelName, title, text, 999);
    }
}
