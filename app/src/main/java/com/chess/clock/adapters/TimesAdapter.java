package com.chess.clock.adapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
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

    private static final String KEY_EDIT_MODE = "times_adapter_edit_mode";
    private static final String KEY_IDS_TO_REMOVE = "times_adapter_ids_to_remove_key";

    ArrayList<TimeControlWrapper> data;
    AppTheme theme;

    long selectedItemId;
    boolean editMode = false;

    private final Set<Long> removeIds = new HashSet<>();
    private final SelectedItemListener itemsListener;

    public TimesAdapter(
            ArrayList<TimeControlWrapper> data,
            long selectedItemId,
            AppTheme theme, SelectedItemListener listener) {
        this.data = data;
        this.selectedItemId = selectedItemId;
        this.theme = theme;
        this.itemsListener = listener;
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
        holder.setUpView(timeControlWrapper, theme, editMode, selectedItemId, removeIds);
        long id = timeControlWrapper.getId();
        holder.itemView.setOnClickListener(v -> {
            if (editMode) {
                if (removeIds.contains(id)) {
                    removeIds.remove(id);
                } else {
                    removeIds.add(id);
                }
                itemsListener.onMarkItemToRemove(removeIds.size());
            } else {
                selectedItemId = id;
                itemsListener.onSelectedItemChange(selectedItemId);
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

    public Set<Long> getIdsToRemove() {
        return removeIds;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateTheme(AppTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setEditMode(Boolean editMode) {
        this.editMode = editMode;
        notifyDataSetChanged();
    }

    public boolean inEditMode() {
        return editMode;
    }

    public void saveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_EDIT_MODE, editMode);
        long[] removeIdsArray = new long[removeIds.size()];
        int idx = 0;
        for (long id : removeIds) {
            removeIdsArray[idx++] = id;
        }
        outState.putLongArray(KEY_IDS_TO_REMOVE, removeIdsArray);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        editMode = savedInstanceState.getBoolean(KEY_EDIT_MODE);
        removeIds.clear();
        for (long id : savedInstanceState.getLongArray(KEY_IDS_TO_REMOVE)) {
            removeIds.add(id);
        }
    }

    public void clearRemoveIds() {
        removeIds.clear();
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

        public void setUpView(
                TimeControlWrapper timeControlWrapper,
                AppTheme theme,
                boolean editMode,
                long selectedItemId,
                Set<Long> removeIds) {
            nameTv.setText(timeControlWrapper.getTimeControlPlayerOne().getName());
            if (editMode) {
                checkBox.setChecked(removeIds.contains(timeControlWrapper.getId()));
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

        void onMarkItemToRemove(int removeItemsCount);
    }
}
