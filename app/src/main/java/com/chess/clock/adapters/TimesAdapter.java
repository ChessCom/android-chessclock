package com.chess.clock.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;

import java.util.ArrayList;
import java.util.Collections;

public class TimesAdapter extends RecyclerView.Adapter<TimesAdapter.TimeItemViewHolder> implements TimeRowMoveCallback.TimeItemTouchCallback {

    ArrayList<TimeControlWrapper> data;
    AppTheme theme;

    public TimesAdapter(ArrayList<TimeControlWrapper> data, AppTheme theme) {
        this.data = data;
        this.theme = theme;
    }

    @NonNull
    @Override
    public TimeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_time, parent, false);
        return new TimeItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeItemViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onTimeItemMoved(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
        notifyItemMoved(from, to);
    }

    public static class TimeItemViewHolder extends RecyclerView.ViewHolder {

        AppCompatCheckedTextView nameTv;

        public TimeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
        }

        public void bind(TimeControlWrapper timeControlWrapper) {
            nameTv.setText(timeControlWrapper.getTimeControlPlayerOne().getName());
        }
    }
}
