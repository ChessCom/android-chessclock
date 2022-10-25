package com.chess.clock.adapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckedTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.views.ViewUtils;

import java.util.ArrayList;
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
                notifyItemChanged(position);
            } else {
                int oldSelectedItemPosition = getSelectedItemPosition();
                selectedItemId = id;
                itemsListener.onSelectedItemChange(selectedItemId);
                notifyItemChanged(position);
                notifyItemChanged(oldSelectedItemPosition);
            }
        });
        holder.editButton.setOnClickListener(v -> itemsListener.onClickEdit(timeControlWrapper));
        holder.itemView.setOnLongClickListener(v -> {
                    boolean consumeLongClick = !editMode;
                    if (consumeLongClick) {
                        itemsListener.onItemLongClick();
                    }
                    return consumeLongClick;
                }
        );

    }

    private int getSelectedItemPosition() {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == selectedItemId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onTimeItemMoved(int from, int to) {
        itemsListener.onItemsReordered(from, to);
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

    public TimeControlWrapper getSelectedTimeControlWrapper() {
        for (TimeControlWrapper wrapper : data) {
            if (wrapper.getId() == selectedItemId) {
                return wrapper;
            }
        }
        return null;
    }

    public static class TimeItemViewHolder extends RecyclerView.ViewHolder {

        AppCompatCheckedTextView nameTv;
        ImageView checkBoxImg;
        View editButton;
        View reorderButton;

        public TimeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.nameTv);
            checkBoxImg = itemView.findViewById(R.id.checkBoxImg);
            editButton = itemView.findViewById(R.id.editBtn);
            reorderButton = itemView.findViewById(R.id.reorderBtn);
        }

        public void setUpView(
                TimeControlWrapper timeControlWrapper,
                AppTheme theme,
                boolean editMode,
                long selectedItemId,
                Set<Long> removeIds) {
            nameTv.setText(timeControlWrapper.getTimeControlPlayerOne().getName());
            if (editMode) {

                boolean selectedToRemove = removeIds.contains(timeControlWrapper.getId());
                if (selectedToRemove) {
                    checkBoxImg.setImageResource(R.drawable.ic_check_box);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkBoxImg.setImageTintList(theme.primaryColorAsStateList(checkBoxImg.getContext()));
                    }
                } else {
                    checkBoxImg.setImageResource(R.drawable.ic_check_box_frame);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        checkBoxImg.setImageTintList(null);
                    }
                }
                nameTv.setCheckMarkDrawable(null);
            } else {
                nameTv.setCheckMarkDrawable(R.drawable.list_radio_button_selector);
                nameTv.setChecked(selectedItemId == timeControlWrapper.getId());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    nameTv.setCheckMarkTintList(theme.colorStateListChecked(nameTv.getContext()));
                }
            }
            reorderButton.setOnClickListener(v -> itemView.performLongClick());
            ViewUtils.showView(checkBoxImg, editMode);
            ViewUtils.showView(editButton, editMode);
            ViewUtils.showView(reorderButton, editMode);
        }
    }

    public interface SelectedItemListener {
        void onSelectedItemChange(long itemId);

        void onMarkItemToRemove(int removeItemsCount);

        void onClickEdit(TimeControlWrapper wrapper);

        void onItemsReordered(int from, int to);

        void onItemLongClick();
    }
}
