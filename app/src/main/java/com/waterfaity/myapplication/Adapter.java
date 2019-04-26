package com.waterfaity.myapplication;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    private Context context;
    private ArrayList<MusicPlayService.MusicBean> dataList;

    public Adapter(Context context, ArrayList<MusicPlayService.MusicBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    public ArrayList<MusicPlayService.MusicBean> getDataList() {
        return dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onItemClick(i);
            }
        });
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onClickListener.onItemLongClick(i);
                return false;
            }
        });
        TextView textView = viewHolder.itemView.findViewById(R.id.text);
        textView.setText(dataList.get(i).getMp3UrlOrPath());
        textView.setTextColor(Color.BLACK);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private OnItemClickListener onClickListener;

    public interface OnItemClickListener {
        void onItemClick(int pos);

        void onItemLongClick(int pos);
    }

    public Adapter setOnClickListener(OnItemClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
}
