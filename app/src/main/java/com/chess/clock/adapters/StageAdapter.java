package com.chess.clock.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.chess.clock.R;
import com.chess.clock.engine.Stage;
import com.chess.clock.engine.StageManager;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeIncrement;
import com.chess.clock.fragments.TimeControlFragment;

// todo remove
public class StageAdapter extends ArrayAdapter<Stage> {

    private final StageManager mStageManager;
    private final TimeIncrement timeIncrement;

    public StageAdapter(Context context, TimeControl timeControl, Fragment targetFragment) {
        super(context, R.layout.list_stage_item, timeControl.getStageManager().getStages());
        mStageManager = timeControl.getStageManager();
        timeIncrement = timeControl.getTimeIncrement();
    }

    @Override
    public int getCount() {
        return mStageManager.getTotalStages();
    }

    @Override
    public Stage getItem(int position) {
        return mStageManager.getStages()[position];
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final StageHolder holder;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(R.layout.list_stage_item, parent, false);

            holder = new StageHolder();
            holder.positionLabel = row.findViewById(R.id.positionTv);
            holder.stageDetails = row.findViewById(R.id.stageDetailsTv);
            holder.timeIncrementDetails = row.findViewById(R.id.incrementDetailsTv);

            row.setTag(holder);
        } else {
            holder = (StageHolder) row.getTag();
        }

        Stage stage = mStageManager.getStages()[position];
        holder.positionLabel.setText(String.valueOf(position + 1));
        holder.stageDetails.setText(stage.toString());
        holder.timeIncrementDetails.setText(timeIncrement.toString());
        return row;
    }

    static class StageHolder {
        TextView stageDetails;
        TextView timeIncrementDetails;
        TextView positionLabel;
    }
}
