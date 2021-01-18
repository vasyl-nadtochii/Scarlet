package com.example.scarlet;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.scarlet.Services.NotificationActionService;

public class CreateNotification {

    public static final String CHANNEL_ID = "channel1";

    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_NEXT = "action_next";

    static Notification notification;


    static boolean isPlaying;

    public static void createNotification
            (Context context, String songName, String artistName, int playButton, Bitmap icon) {

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");

        PendingIntent pendingIntentPrevious;
        int drw_previous;
        PendingIntent pendingIntentNext;
        int drw_next;

        if(icon == null) {
            icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.album2);
        }

        Intent intentPrevious = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PREVIOUS);
        pendingIntentPrevious = PendingIntent.getBroadcast(context, 0,
                intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_previous = R.drawable.ic_prev;

        Intent intentNext= new Intent(context, NotificationActionService.class)
                .setAction(ACTION_NEXT);
        pendingIntentNext = PendingIntent.getBroadcast(context, 0,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        drw_next = R.drawable.ic_next;

        Intent intentPlay = new Intent(context, NotificationActionService.class)
                .setAction(ACTION_PLAY);
        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0,
                intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent contentIntent = new Intent(context, MainActivity.class);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 1,
                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        isPlaying = playButton != R.drawable.ic_play;

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note_black_24dp)
            .setContentTitle(songName)
            .setContentText(artistName)
            .setLargeIcon(icon)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(drw_previous, "Previous", pendingIntentPrevious)
            .addAction(playButton, "Play", pendingIntentPlay)
            .addAction(drw_next, "Next", pendingIntentNext)
            .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
                    .setMediaSession(mediaSessionCompat
                            .getSessionToken()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .build();

        notificationManagerCompat.notify(1, notification);
    }
}
