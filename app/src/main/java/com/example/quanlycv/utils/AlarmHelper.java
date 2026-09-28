package com.example.quanlycv.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.quanlycv.model.Task;
import com.example.quanlycv.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmHelper {

    public static void setAlarm(Context context, Task task) {
        if (task == null || task.getDate() == null || task.getTime() == null) {
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(task.getDate() + " " + task.getTime());
            if (date != null) {
                int leadMinutes = task.getReminderLeadTime();
                long leadMs = (leadMinutes > 0) ? (leadMinutes * 60 * 1000L) : 0L;
                long triggerAtMillis = date.getTime() - leadMs;

                if (triggerAtMillis > System.currentTimeMillis()) {
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.putExtra("TASK_ID", task.getId());
                    intent.putExtra("TASK_TITLE", task.getTitle());
                    intent.putExtra("TASK_DESC", task.getDesc());
                    intent.putExtra("TASK_TIME", task.getDate() + " " + task.getTime());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            context,
                            task.getId(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    if (alarmManager != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                if (alarmManager.canScheduleExactAlarms()) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                                } else {
                                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                                }
                            } else {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                            }
                        } catch (SecurityException e) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
                        }
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AlarmHelper", "Error setting alarm", e);
        }
    }

    public static void cancelAlarm(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
