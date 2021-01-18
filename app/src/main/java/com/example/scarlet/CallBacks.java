package com.example.scarlet;

import android.content.Context;
import android.graphics.Bitmap;

public interface CallBacks {
    void updateUI(String songName, String artistName, int duration, Bitmap imgURI);
    void startSeekBarUpdater(Context context);
    void timerControls(String action);
    void redrawPauseBtn(String action);
}
