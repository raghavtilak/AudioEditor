package com.raghav.audioeditor.ListView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.raghav.audioeditor.R;

import java.util.ArrayList;

public class SongAdapter extends ArrayAdapter<SongModel> {

    private ArrayList<SongModel> songArrayList;
    private Context context;
    private SongAdapter.OnMusicItemClickListener musicItemClickListener;

    private int itemLayoutId;

    public SongAdapter(@NonNull Context context, ArrayList<SongModel> songArrayList, OnMusicItemClickListener onMusicItemClickListener) {
        super(context,0, songArrayList);
        this.context=context;
        this.songArrayList=songArrayList;
        itemLayoutId=R.layout.videolist_item_lv;
        this.musicItemClickListener=onMusicItemClickListener;
    }
    public void setItemView(int layoutId) {
        itemLayoutId=layoutId;
    }

    public interface OnMusicItemClickListener {
        void onMusicClick(int position, SongModel musicItem);
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(itemLayoutId, parent, false);
        }
        SongModel item = songArrayList.get(position);
        TextView songName,albumName;
        ImageView ivPlay;
        RelativeLayout rlContainer;
        songName = itemView.findViewById(R.id.music_name);
        albumName = itemView.findViewById(R.id.album_name);
        if(!item.getAlbum().isEmpty()){
            albumName.setText(item.getAlbum());
        }
        ivPlay = itemView.findViewById(R.id.play_media_player);
        rlContainer = itemView.findViewById(R.id.container_music_item);
        ivPlay.setOnClickListener((view) -> {
            musicItemClickListener.onMusicClick(position, item);
        });
        songName.setText(item.getTitle());

        return itemView;
    }

}
