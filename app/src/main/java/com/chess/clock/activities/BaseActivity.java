package com.chess.clock.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.chess.clock.statics.AppData;

public class BaseActivity extends AppCompatActivity {
    /**
     * Shared preferences wrapper
     */
    protected AppData appData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appData = new AppData(getApplicationContext());
    }
}
