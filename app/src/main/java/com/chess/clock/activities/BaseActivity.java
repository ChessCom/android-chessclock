package com.chess.clock.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chess.clock.ClockApplication;
import com.chess.clock.R;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.manager.ChessClockManager;
import com.chess.clock.statics.AppData;
import com.chess.clock.views.ViewUtils;

public class BaseActivity extends AppCompatActivity {
    /**
     * Shared preferences wrapper
     */
    protected AppData appData;

    public AppTheme selectedTheme;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appData = new AppData(getApplicationContext());
        selectedTheme = appData.getSelectedTheme();
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ViewUtils.applyWindowInsets(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.selectedTheme = appData.getSelectedTheme();
    }

    public ChessClockManager getClockManager() {
        return ClockApplication.getClockManager();
    }

    public void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
    }
}
