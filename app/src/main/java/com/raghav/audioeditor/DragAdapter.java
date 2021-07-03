package com.raghav.audioeditor;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.d.lib.pulllayout.rv.adapter.CommonAdapter;
import com.d.lib.pulllayout.rv.adapter.CommonHolder;
import com.d.lib.pulllayout.rv.itemtouchhelper.ItemTouchHelperViewHolder;
import com.raghav.audioeditor.ListView.SongModel;

import java.util.Collections;
import java.util.List;

public class DragAdapter extends CommonAdapter<SongModel> {

    private OnPlayButtonClickListener playButtonClickListener;

    public DragAdapter(@NonNull Context context, List<SongModel> datas, int layoutId, OnPlayButtonClickListener playButtonClickListener) {
        super(context, datas, layoutId);
        this.playButtonClickListener=playButtonClickListener;
    }


    @Override
    public void convert(int position, CommonHolder holder, SongModel v) {

        holder.setText(R.id.music_name,v.getTitle());
        holder.setOnClickListener(R.id.play_media_player,new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonClickListener.onPlayButtonClicked(v);
            }
        });

        holder.setOnItemTouchListener(new ItemTouchHelperViewHolder() {
            @Override
            public void onItemSelected() {
                // Callback when dragging is triggered
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.select_color));
            }

            @Override
            public void onItemClear() {
                // Callback when finger is released
                holder.itemView.setBackgroundColor(0);
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);

    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {

        if (Math.abs(fromPosition - toPosition) > 1) {
            SongModel from = mDatas.get(fromPosition);
            mDatas.remove(fromPosition);
            mDatas.add(toPosition, from);
        } else {
            Collections.swap(mDatas, fromPosition, toPosition);
        }
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    public interface OnPlayButtonClickListener{
        void onPlayButtonClicked(SongModel s);
    }
}
