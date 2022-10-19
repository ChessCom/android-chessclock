package com.chess.clock.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.chess.clock.R;
import com.chess.clock.engine.Stage;

public class StageRowView extends ConstraintLayout {

    TextView stageDetails;
    TextView timeIncrementDetails;
    TextView positionLabel;

    public StageRowView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_stage_item, this, true);

        positionLabel = view.findViewById(R.id.positionTv);
        stageDetails = view.findViewById(R.id.stageDetailsTv);
        timeIncrementDetails = view.findViewById(R.id.incrementDetailsTv);
    }

    public void updateData(int position, Stage stage) {
        positionLabel.setText(String.valueOf(position));
        String details = "";
        String timeFormatted = stage.durationTimeFormatted();
        if (stage.getStageType() == Stage.StageType.GAME) {
            details = getContext().getString(R.string.game_in_x, timeFormatted);
        } else {
            details = getResources().getQuantityString(R.plurals.x_moves_in, stage.getTotalMoves(), stage.getTotalMoves(), timeFormatted);
        }
        stageDetails.setText(details);
        timeIncrementDetails.setText(stage.getTimeIncrement().toString());
    }
}
