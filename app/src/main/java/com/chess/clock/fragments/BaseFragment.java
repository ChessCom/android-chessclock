package com.chess.clock.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chess.clock.activities.BaseActivity;
import com.chess.clock.entities.AppTheme;

/**
 * BaseFragment requires BaseActivity as a parent to load theme.
 */
public abstract class BaseFragment extends Fragment {
    AppTheme loadedTheme;
    boolean shouldReloadTheme = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        shouldReloadTheme = true;
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTheme activityTheme = getBaseActivity().selectedTheme;

        if (activityTheme == null) return;

        if (activityTheme != loadedTheme || shouldReloadTheme) {
            loadedTheme = activityTheme;
            loadTheme(loadedTheme);
            shouldReloadTheme = false;
        }
    }

    @NonNull
    private BaseActivity getBaseActivity() {
        return (BaseActivity) requireActivity();
    }

    abstract void loadTheme(AppTheme theme);
}
