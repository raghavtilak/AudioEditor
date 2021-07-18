package com.raghav.audioeditor.CustomRV;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.raghav.audioeditor.DragAdapter;
import com.raghav.audioeditor.ListView.SongModel;
import com.raghav.audioeditor.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements ItemTouchHelperAdapter{
    private List<SongModel> imageList = new ArrayList<>();
    private Context context;
//    private OnClickListener onClickListener;
    private OnPlayButtonClickListener playButtonClickListener;
    private int layoutId;
    /**
     * Can be used when loading more
     *
     * @param dataList
     */
    public void addData(List<SongModel> dataList) {
        if (dataList != null) {
            imageList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    public List<SongModel> getData() {
        return imageList;
    }

    /**
     * Update Adapter
     *
     * @param dataList
     */
    public void replaceData(List<SongModel> dataList) {
        if (dataList != null) {
            imageList.clear();
            addData(dataList);
        }
    }

    public RecyclerViewAdapter(@NonNull Context context, List<SongModel> datas, int layoutId, OnPlayButtonClickListener playButtonClickListener) {
        this.context=context;
        this.imageList=datas;
//        this.onClickListener=onClickListener;
        this.playButtonClickListener=playButtonClickListener;
        this.layoutId=layoutId;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SongModel v = imageList.get(position);
        holder.textView.setText(v.getTitle());
        holder.iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonClickListener.onPlayButtonClicked(v);
            }
        });
        holder.duration.setText(v.getDuration());
//        Glide.with(context)
//                .load(v.getUri())
//                .override(200,200)
//                .centerCrop()
//                .placeholder(R.drawable.ic_launcher_foreground)
//                .into(holder.iv);
//
//        holder.textView.setText(timeConversion(v.getDuration()*1000));
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onClickListener.onClick(position,v);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    @Override
    public void onMove(int fromPosition, int toPosition) {
        /**
         * Move to the original array data here
         */
        Collections.swap(imageList, fromPosition, toPosition);
        /**
         * Notification data movement
         */
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onSwipe(int position) {
        /**
         * Original data removal data
         */
        imageList.remove(position);
        /**
         * Notification removal
         */
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView,duration;
        private ImageView iv;

        public ViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.play_media_player);
            textView = (TextView) itemView.findViewById(R.id.music_name);
            duration = (TextView) itemView.findViewById(R.id.album_name);


        }
    }


    public interface OnClickListener{
        void onClick(int position,SongModel item);
    }
    public interface OnPlayButtonClickListener{
        void onPlayButtonClicked(SongModel s);
    }
}
