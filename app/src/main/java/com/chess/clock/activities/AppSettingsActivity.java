package com.chess.clock.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.adapters.ThemesAdapter;

public class AppSettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recycler = findViewById(R.id.themesRecycler);
        recycler.setAdapter(new ThemesAdapter());

    }
}