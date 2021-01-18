package com.example.scarlet.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

import com.example.scarlet.CreateNotification;
import com.example.scarlet.MainActivity;
import com.example.scarlet.PlaybackControls;
import com.example.scarlet.R;
import com.example.scarlet.Track;

import java.util.Objects;

import static com.example.scarlet.MainActivity.tracklist;

public class MusicService extends Service implements PlaybackControls {

    public static int trackPos;
    public static boolean isRepeating = false;

    public static MediaPlayer mediaPlayer;
    public static NotificationManager notificationManager;
    private final IBinder musicBind = new MusicBinder();
    TelephonyManager mgr;

    public Track currentTrack;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return startId;
    }

    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CreateNotification.CHANNEL_ID,
                "Scarlet", NotificationManager.IMPORTANCE_LOW);

        notificationManager = getSystemService(NotificationManager.class);

        registerReceiver(broadcastReceiver, new IntentFilter("TRACKS_TRACKS"));
        registerReceiver(mNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if(mgr != null) {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        if(notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    public void prepareUpdater(Track track, Context context) {
        if(mediaPlayer != null) {
            if(!mediaPlayer.isPlaying()) {
                MainActivity.callBacks.redrawPauseBtn("SET_PAUSE_DR");
            }

            mediaPlayer.stop();
            mediaPlayer.release();
        }

        currentTrack = track;

        Intent intent = new Intent("UPDATE");
        sendBroadcast(intent);

        Uri u = Uri.parse(track.getID());
        mediaPlayer = MediaPlayer.create(context, u);

        MainActivity.callBacks.updateUI(track.getSongName(), track.getSongArtist(),
                mediaPlayer.getDuration(), track.getAlbumImg());

        CreateNotification.createNotification(context,
                track.getSongName(), track.getSongArtist(),
                R.drawable.ic_pause, track.getAlbumImg());

        mediaPlayer.start();
        MainActivity.callBacks.startSeekBarUpdater(getApplication());
    }

    @Override
    public void nextTrack(Context context) {
        trackPos = (trackPos + 1) % tracklist.size();

        prepareUpdater(tracklist.get(trackPos), context);
    }

    @Override
    public void playAndPause(Context context) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            MainActivity.callBacks.timerControls("STOP");

            CreateNotification.createNotification(context,
                    tracklist.get(trackPos).getSongName(), tracklist.get(trackPos).getSongArtist(),
                    R.drawable.ic_play, tracklist.get(trackPos).getAlbumImg());

            MainActivity.callBacks.redrawPauseBtn("SET_PLAY_DR");
        }
        else {
            MainActivity.callBacks.redrawPauseBtn("SET_PAUSE_DR");
            CreateNotification.createNotification(context,
                    tracklist.get(trackPos).getSongName(), tracklist.get(trackPos).getSongArtist(),
                    R.drawable.ic_pause,  tracklist.get(trackPos).getAlbumImg());

            try {
                mediaPlayer.start();
                MainActivity.callBacks.timerControls("START");
                MainActivity.callBacks.startSeekBarUpdater(context);
            }
            catch (IllegalStateException e) {
                mediaPlayer.pause();
                MainActivity.callBacks.timerControls("STOP");
            }
        }
    }

    @Override
    public void prevTrack(Context context) {
        if(mediaPlayer.getCurrentPosition() <= 3000)
            trackPos = ((trackPos - 1) < 0) ? (tracklist.size() - 1) : (trackPos - 1);

        prepareUpdater(tracklist.get(trackPos), context);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mediaPlayer != null && mediaPlayer.isPlaying() ) {
                playAndPause(getApplication());
            }
        }
    };

    // Incoming call receiver
    PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (state == TelephonyManager.CALL_STATE_RINGING
                    && mediaPlayer != null
                    && mediaPlayer.isPlaying()) {

                playAndPause(getApplication());

            }
            super.onCallStateChanged(state, incomingNumber);
        }
    };

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String actions = Objects.requireNonNull(intent.getExtras()).getString("actionName");

            assert actions != null;
            switch (actions){
                case CreateNotification.ACTION_PREVIOUS:
                    prevTrack(getApplication());
                    break;
                case CreateNotification.ACTION_PLAY:
                    playAndPause(getApplication());
                    break;
                case CreateNotification.ACTION_NEXT:
                    nextTrack(getApplication());
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancelAll();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(mNoisyReceiver);
        mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

        super.onDestroy();
    }
}
