package com.example.scarlet;

import android.content.Context;

public interface PlaybackControls {
    void nextTrack(Context context);
    void playAndPause(Context context);
    void prevTrack(Context context);
}
