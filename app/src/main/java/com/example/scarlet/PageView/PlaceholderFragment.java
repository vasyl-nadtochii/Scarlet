package com.example.scarlet.PageView;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.scarlet.MainActivity;
import com.example.scarlet.R;
import com.example.scarlet.Services.MusicService;
import com.example.scarlet.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    public TrackListAdapter trackListAdapter;

    public View view1, root;
    public ListView trackListView;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);

        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_main, container, false);

        trackListView = root.findViewById(R.id.trackList);
        view1 = root.findViewById(R.id.view1);

        if (pageViewModel.getIndex() == 1) {
            trackListView.setVisibility(View.VISIBLE);
            view1.setVisibility(View.GONE);
        } else {
            view1.setVisibility(View.VISIBLE);
            trackListView.setVisibility(View.GONE);
        }

        initViews();

        return root;
    }

    private BroadcastReceiver updater = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            trackListView.invalidateViews();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(updater,
                new IntentFilter("UPDATE"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unregisterReceiver(updater);
    }

    public void initViews() {
        final ArrayList<Track> tracklist = MainActivity.getTrackList();

        trackListAdapter = new TrackListAdapter(tracklist, getActivity());
        trackListView.setAdapter(trackListAdapter);

        trackListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int clickPos, long id) {
                MusicService.trackPos = clickPos;
                MainActivity.musicSrv.prepareUpdater(tracklist.get(clickPos), getActivity());
            }
        });
    }

    public class TrackListAdapter extends ArrayAdapter<Track> {

        private List<Track> iTrackList;
        private Context context;

        TrackListAdapter(@NonNull List<Track> trackList, Context context) {
            super(context, R.layout.track_item, trackList);
            this.iTrackList = trackList;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("ViewHolder")
            View item = layoutInflater.inflate(R.layout.track_item, parent, false);

            TextView songName = item.findViewById(R.id.songName);
            TextView artistName = item.findViewById(R.id.artistName);
            View divider = item.findViewById(R.id.divider);
            ImageView note = item.findViewById(R.id.note);

            songName.setText(iTrackList.get(pos).getSongName());
            artistName.setText(iTrackList.get(pos).getSongArtist());

            if(pos == iTrackList.size() - 1) {
                divider.setVisibility(View.GONE);
            }

            if(pos == MusicService.trackPos && MusicService.mediaPlayer != null) {
                note.setVisibility(View.VISIBLE);
                songName.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            return item;
        }
    }
}