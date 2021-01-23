package com.example.scarlet;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.slidingpanelayout.widget.SlidingPaneLayout;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.scarlet.PageView.SectionsPagerAdapter;
import com.example.scarlet.Services.MusicService;
import com.google.android.material.tabs.TabLayout;
import com.jgabrielfreitas.core.BlurImageView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.example.scarlet.Services.MusicService.isRepeating;
import static com.example.scarlet.Services.MusicService.mediaPlayer;
import static com.example.scarlet.Services.MusicService.trackPos;


public class MainActivity extends AppCompatActivity
        implements SlidingPaneLayout.PanelSlideListener {

    TextView songNameLabel, songArtistLabel, smallNameLabel, smallArtistLabel;
    SlidingUpPanelLayout slider;
    ViewGroup smallPlayer;
    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    View view;
    ImageView image, smallImage, pause;
    SeekBar seekBar;
    Chronometer timer;
    Button next, prev, smallPause, repeatSet, sk_forward, sk_backward;
    BlurImageView bgImg;
    EditText editText;

    SlidingUpPanelLayout.PanelState zeroPanelState;
    Animation fadeOut, fadeIn;
    Bitmap default_ic;

    boolean firstLaunch = true;
    public static int playingPos;

    public static MusicService musicSrv;
    public static CallBacks callBacks;

    private Handler animationHandler = new Handler();
    private MediaMetadataRetriever metadataRetriever;
    public static ArrayList<Track> tracklist;
    private final static Handler handler = new Handler();
    private Intent playIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        runtimePermission();
    }

    public void runtimePermission() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        initViews();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        finishAndRemoveTask();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown
                            (PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    public void initViews() {
        playIntent = new Intent(this, MusicService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);

        default_ic = BitmapFactory.decodeResource(this.getResources(), R.drawable.album2);

        metadataRetriever = new MediaMetadataRetriever();
        tracklist = findTrack(Environment.getExternalStorageDirectory());

        Collections.sort(tracklist, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                return o1.getSongName().compareTo(o2.getSongName());
            }
        });

        sectionsPagerAdapter =
                new SectionsPagerAdapter(this, getSupportFragmentManager());

        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        slider = findViewById(R.id.slider);
        slider.setTouchEnabled(false);  //temporary !

        view = findViewById(R.id.whiteView);

        editText = findViewById(R.id.edit_txt);

        image = findViewById(R.id.image);
        bgImg = findViewById(R.id.bg_image);

        songNameLabel = findViewById(R.id.songName);
        songArtistLabel = findViewById(R.id.artistName);

        seekBar = findViewById(R.id.seekBar);
        seekBar.getProgressDrawable().setColorFilter(Color.parseColor("#ffffff"),
                PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(Color.parseColor("#ffffff"),
                PorterDuff.Mode.SRC_IN);

        timer = findViewById(R.id.timer);

        pause = findViewById(R.id.pause);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);

        repeatSet = findViewById(R.id.repeat);

        sk_backward = findViewById(R.id.back5);
        sk_forward = findViewById(R.id.forward5);

        sk_forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePlayingPos(mediaPlayer.getCurrentPosition() + 5000);
            }
        });

        sk_backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePlayingPos(mediaPlayer.getCurrentPosition() - 5000);
            }
        });

        smallPlayer = findViewById(R.id.smallPlayer);
        smallNameLabel = findViewById(R.id.smallSongName);
        smallArtistLabel = findViewById(R.id.smallSongArtist);
        smallPause = findViewById(R.id.smallPause);
        smallImage = findViewById(R.id.smallImage);

        callBacks = new CallBacks() {
            @Override
            public void updateUI(String songName, String songArtist, int duration, Bitmap imgURI) {
                if(!slider.isTouchEnabled()) {
                    slider.setTouchEnabled(true);       //temporary !
                }

                songNameLabel.setText(songName);
                songArtistLabel.setText(songArtist);

                smallNameLabel.setText(songName);
                smallArtistLabel.setText(songArtist);

                songNameLabel.setSelected(true);
                songArtistLabel.setSelected(true);

                try {
                    image.setImageBitmap(imgURI);
                    smallImage.setImageBitmap(imgURI);
                    bgImg.setImageBitmap(imgURI);
                } catch (Exception e) {
                    image.setImageBitmap(default_ic);
                    smallImage.setImageBitmap(default_ic);
                    bgImg.setImageBitmap(default_ic);
                }

                bgImg.setBlur(5);

                seekBar.setMax(duration);
            }

            @Override
            public void startSeekBarUpdater(final Context context) {
                playingPos = mediaPlayer.getCurrentPosition();

                timer.setBase(SystemClock.elapsedRealtime() - mediaPlayer.getCurrentPosition() - 500);
                seekBar.setProgress(playingPos);

                if (mediaPlayer.isPlaying()) {
                    Runnable notification = new Runnable() {
                        @Override
                        public void run() {
                            startSeekBarUpdater(context);
                        }
                    };
                    handler.postDelayed(notification, 500);
                } else if (!mediaPlayer.isPlaying() &&
                        mediaPlayer.getCurrentPosition() < mediaPlayer.getDuration() - 3000) {
                    mediaPlayer.pause();
                    timer.stop();
                } else {
                    if (isRepeating) {
                        musicSrv.prepareUpdater(tracklist.get(trackPos), context);
                    } else {
                        musicSrv.nextTrack(getApplication());
                    }
                }
            }

            @Override
            public void timerControls(String action) {
                switch (action) {
                    case "START":
                        timer.start();
                        break;
                    case "STOP":
                        timer.stop();
                        break;
                }
            }

            @Override
            public void redrawPauseBtn(String action) {
                AnimatedVectorDrawable avDr;
                Drawable drawable;

                switch (action) {
                    case "SET_PAUSE_DR":
                        pause.setImageDrawable( ResourcesCompat
                                .getDrawable( getResources(), R.drawable.avd_play_to_pause, getTheme() ) );

                        drawable = pause.getDrawable();
                        if (drawable instanceof AnimatedVectorDrawable) {
                            avDr = (AnimatedVectorDrawable) drawable;
                            avDr.start();
                        }

                        smallPause.setBackgroundResource(R.drawable.ic_pause);
                        break;
                    case "SET_PLAY_DR":
                        pause.setImageDrawable( ResourcesCompat
                                .getDrawable( getResources(), R.drawable.avd_pause_to_play, getTheme() ) );

                        drawable = pause.getDrawable();
                        if (drawable instanceof AnimatedVectorDrawable) {
                            avDr = (AnimatedVectorDrawable) drawable;
                            avDr.start();
                        }

                        smallPause.setBackgroundResource(R.drawable.ic_play);
                        break;
                }
            }
        };

        if (mediaPlayer != null) {
            callBacks.updateUI(
                    musicSrv.currentTrack.getSongName(),
                    musicSrv.currentTrack.getSongArtist(),
                    mediaPlayer.getDuration(),
                    musicSrv.currentTrack.getAlbumImg());

            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            callBacks.startSeekBarUpdater(getApplication());
        }

        fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);

        zeroPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;

        repeatSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRepeating) {
                    isRepeating = true;
                    repeatSet.setBackgroundResource(R.drawable.ic_repeat_active);
                } else {
                    isRepeating = false;
                    repeatSet.setBackgroundResource(R.drawable.ic_repeat_black_24dp);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    musicSrv.nextTrack(getApplication());
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    musicSrv.prevTrack(getApplication());
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    musicSrv.playAndPause(getApplication());
            }
        });

        smallPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    slider.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        smallPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null)
                    musicSrv.playAndPause(getApplication());
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null)
                    changePlayingPos(seekBar.getProgress());
            }
        });

        slider.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {

            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slider.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
                    smallPlayer.setVisibility(View.GONE);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {

                if (firstLaunch && newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    fadeOut();
                    smallPlayer.setVisibility(View.GONE);

                    firstLaunch = false;
                } else if (!firstLaunch && zeroPanelState == SlidingUpPanelLayout.PanelState.COLLAPSED
                        && newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {

                    zeroPanelState = SlidingUpPanelLayout.PanelState.EXPANDED;
                    fadeIn();
                    smallPlayer.setVisibility(View.VISIBLE);
                } else if (!firstLaunch && zeroPanelState == SlidingUpPanelLayout.PanelState.EXPANDED
                        && newState == SlidingUpPanelLayout.PanelState.EXPANDED) {

                    zeroPanelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
                    fadeOut();
                    smallPlayer.setVisibility(View.GONE);
                }
            }
        });
    }

    public void changePlayingPos(int pos) {
        mediaPlayer.seekTo(pos);
        timer.setBase(SystemClock.elapsedRealtime() - mediaPlayer.getCurrentPosition());
    }

    public static ArrayList<Track> getTrackList() {
        return tracklist;
    }

    public ArrayList<Track> findTrack(File file) {

        ArrayList<Track> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        assert files != null;
        for (File singleFile: files) {
            if(singleFile.getName().endsWith(".mp3") && !singleFile.isDirectory()) {
                metadataRetriever.setDataSource(singleFile.getPath());

                String songName = metadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artistName = metadataRetriever
                        .extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                if (songName == null) {
                    songName = singleFile
                            .getName()
                            .replace(".mp3", "");
                }
                if (artistName == null) {
                    artistName = "Unknown Artist";
                }

                String id = singleFile.toString();

                byte[] albumURI = metadataRetriever.getEmbeddedPicture();

                Bitmap art;
                if (albumURI != null)
                    art = BitmapFactory.decodeByteArray(albumURI, 0, albumURI.length);
                else
                    art = default_ic;

                arrayList.add(new Track(songName, artistName, id, art));
            }
            else if (singleFile.isDirectory() && !singleFile.isHidden()) {
                arrayList.addAll(findTrack(singleFile));
            }
        }

        return arrayList;
    }

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public void fadeIn() {
        view.startAnimation(fadeIn);
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, 175);
    }

    public void fadeOut() {
        view.startAnimation(fadeOut);
        animationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.GONE);
            }
        }, 175);
    }

    @Override
    public void onPanelSlide(@NonNull View panel, float slideOffset) { }

    @Override
    public void onPanelOpened(@NonNull View panel) { }

    @Override
    public void onPanelClosed(@NonNull View panel) { }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
