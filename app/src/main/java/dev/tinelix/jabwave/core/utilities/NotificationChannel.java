package dev.tinelix.jabwave.core.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationChannel {
    private final Context ctx;
    private final String id;
    private final String name;
    private android.app.NotificationChannel channel;
    private NotificationManager manager;
    private boolean ledIndicate;
    private boolean vibrate;
    private boolean playSound;

    public NotificationChannel(Context ctx, String id, String name, boolean ledIndicate,
                               boolean vibrate, boolean playSound, String ringtoneUrl) {
        this.ctx = ctx;
        this.id = id;
        this.name = name;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = ctx.getSystemService(android.app.NotificationManager.class);
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            channel = new android.app.NotificationChannel(id, name, importance);
            channel.enableLights(ledIndicate);
            channel.enableVibration(vibrate);
            if(playSound) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build();
                if(ringtoneUrl != null) {
                    channel.setSound(Uri.parse(ringtoneUrl), audioAttributes);
                } else {
                    channel.setSound(null, null);
                }
            }
            manager.createNotificationChannel(channel);
        } else {
            manager = (android.app.NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public void broadcast(Notification notification, boolean isCancelable) {
        if(isCancelable) {
            notification.flags |= Notification.FLAG_NO_CLEAR;
        }
        manager.notify(0, notification);
    }

    public Notification createNotification(int icon, String title, String description) {
        Notification notification;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder =
                    new Notification.Builder(ctx, id)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description);
            notification = builder.build();
        } else {
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(ctx, id)
                            .setSmallIcon(icon)
                            .setContentTitle(title)
                            .setContentText(description);

            notification = builder.build();

            if(ledIndicate) notification.defaults = Notification.DEFAULT_LIGHTS;
            if(vibrate) notification.defaults = Notification.DEFAULT_VIBRATE;
            if(playSound) notification.defaults = Notification.DEFAULT_SOUND;
        }
        return notification;
    }

    public static class Builder {

        private final Context ctx;
        private String id;
        private String name;
        private boolean ledIndicate;
        private boolean vibrate;
        private boolean playSound;
        private String ringtoneUrl;

        private Builder(Context ctx) {
            this.ctx = ctx;
        }

        public static Builder getInstance(Context ctx) {
            return new Builder(ctx);
        }

        public Builder setChannelName(String id, String name) {
            this.id = id;
            this.name = name;
            return this;
        }

        public Builder setChannelParameters(boolean ledIndicate, boolean vibrate, boolean playSound) {
            this.ledIndicate = ledIndicate;
            this.vibrate = vibrate;
            this.playSound = playSound;
            return this;
        }

        public Builder setRingtoneUrl(String ringtoneUrl) {
            if(playSound) {
                if (ringtoneUrl.equals("content://settings/system/notification_sound")) {
                    this.ringtoneUrl = ringtoneUrl;
                }
            }
            return this;
        }

        public NotificationChannel build() {
            return new NotificationChannel(
                    ctx, id, name, ledIndicate, vibrate, playSound, ringtoneUrl
            );
        }
    }
}
