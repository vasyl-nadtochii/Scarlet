package com.example.scarlet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

class FolderListAdapter extends ArrayAdapter<Folder> {

    private List<Folder> iFolderList;
    private Context context;

    FolderListAdapter(@NonNull List<Folder> folderList, Context context) {
        super(context, R.layout.folder_item, folderList);
        this.iFolderList = folderList;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int pos, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("ViewHolder")
        View item = layoutInflater.inflate(R.layout.folder_item, parent, false);
        TextView folderName = item.findViewById(R.id.folderName);
        TextView folderPath = item.findViewById(R.id.folderPath);
        View divider = item.findViewById(R.id.divider);

        folderName.setText(iFolderList.get(pos).getName());
        folderPath.setText(iFolderList.get(pos).getPath());

        if(pos == iFolderList.size() - 1) {
            divider.setVisibility(View.GONE);
        }

        return item;
    }
}
