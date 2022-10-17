package com.chess.clock.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TimeRowMoveCallback extends ItemTouchHelper.Callback {

    TimeItemTouchCallback touchCallback;

    public TimeRowMoveCallback(TimeItemTouchCallback callback) {
        this.touchCallback = callback;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int drag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(drag, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        this.touchCallback.onTimeItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    public interface TimeItemTouchCallback {
        void onTimeItemMoved(int from, int to);
    }
}
