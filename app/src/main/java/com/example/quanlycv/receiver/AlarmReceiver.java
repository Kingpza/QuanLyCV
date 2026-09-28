package com.example.quanlycv.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.quanlycv.MainActivity;
import com.example.quanlycv.R;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_REMINDER_CHANNEL";
    private static final String CHANNEL_NAME = "Thông báo công việc";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskId = intent.getIntExtra("TASK_ID", -1);
        String rawTitle = intent.getStringExtra("TASK_TITLE");
        String taskTitle = (rawTitle != null) ? rawTitle : "Đã đến giờ làm việc!";
        String taskDesc = intent.getStringExtra("TASK_DESC");
        String taskTime = intent.getStringExtra("TASK_TIME");

        String baseContent = "Đã đến giờ làm việc " + taskTitle;
        String contentText = (taskTime != null && !taskTime.isEmpty()) ? baseContent + " (" + taskTime + ")" : baseContent;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh phát thông báo nhắc nhở đến giờ làm việc");
            channel.enableVibration(true);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                taskId,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Nhắc nhở công việc")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText + (taskDesc != null && !taskDesc.isEmpty() ? "\nMô tả: " + taskDesc : "")))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(taskId > 0 ? taskId : (int) System.currentTimeMillis(), builder.build());
        }
    }
}
