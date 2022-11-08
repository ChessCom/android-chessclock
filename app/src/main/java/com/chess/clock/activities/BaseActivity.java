package com.chess.clock.activities;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chess.clock.R;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.statics.AppData;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectedTheme = appData.getSelectedTheme();
    }

    public void hideStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void showStatusBar() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    public void finishWithAnimation() {
        finish();
        overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
    }
}
