package com.chess.clock.adapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.views.ViewUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TimesAdapter extends RecyclerView.Adapter<TimesAdapter.TimeItemViewHolder> implements TimeRowMoveCallback.TimeItemTouchCallback {

    ArrayList<TimeControlWrapper> data;
    AppTheme theme;
    boolean editMode = false;
    long selectedItemId = -1;
    Set<Long> removeIds = new HashSet<>();

    private final SelectedItemListener selectedItemListener;

    public TimesAdapter(ArrayList<TimeControlWrapper> data, AppTheme theme, SelectedItemListener listener) {
        this.data = data;
        this.theme = theme;
        this.selectedItemListener = listener;
    }

    @NonNull
    @Override
    public TimeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_time, parent, false);
        return new TimeItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeItemViewHolder holder, int position) {
        TimeControlWrapper timeControlWrapper = data.get(position);
        holder.setUpView(timeControlWrapper, theme, editMode, selectedItemId);
        long id = timeControlWrapper.getId();
        holder.itemView.setOnClickListener(v -> {
            if (editMode) {
                if (removeIds.contains(id)) {
                    removeIds.remove(id);
                } else {
                    removeIds.add(id);
                }
            } else {
                selectedItemId = id;
                selectedItemListener.onSelectedItemChange(selectedItemId);
            }
            notifyDataSetChanged();
        });
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateTheme(AppTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    public static class TimeItemViewHolder extends RecyclerView.ViewHolder {

        AppCompatCheckedTextView nameTv;
        AppCompatCheckBox checkBox;
        View editButton;

        public TimeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            checkBox = itemView.findViewById(R.id.editCheckBox);
            editButton = itemView.findViewById(R.id.editBtn);
        }

        public void setUpView(TimeControlWrapper timeControlWrapper, AppTheme theme, boolean editMode, long selectedItemId) {
            nameTv.setText(timeControlWrapper.getTimeControlPlayerOne().getName());
            if (editMode) {
                nameTv.setCheckMarkDrawable(null);
                CompoundButtonCompat.setButtonTintList(checkBox, theme.colorStateListChecked(nameTv.getContext()));
            } else {
                nameTv.setCheckMarkDrawable(R.drawable.list_radio_button_selector);
                nameTv.setChecked(selectedItemId == timeControlWrapper.getId());
                CompoundButtonCompat.setButtonTintList(checkBox, theme.colorStateListChecked(nameTv.getContext()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    nameTv.setCheckMarkTintList(theme.colorStateListChecked(nameTv.getContext()));
                }
            }
            ViewUtils.showView(checkBox, editMode);
            ViewUtils.showView(editButton, editMode);
        }
    }

    public interface SelectedItemListener {
        void onSelectedItemChange(long itemId);
    }
}
