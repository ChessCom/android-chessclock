package com.chess.clock.fragments;

import androidx.fragment.app.Fragment;

import com.chess.clock.activities.BaseActivity;
import com.chess.clock.entities.AppTheme;

/**
 * BaseFragment requires BaseActivity as a parent to load theme.
 */
public abstract class BaseFragment extends Fragment {
    private AppTheme loadedTheme;

    @Override
    public void onResume() {
        super.onResume();
        AppTheme activityTheme = ((BaseActivity) requireActivity()).selectedTheme;
        if (activityTheme != loadedTheme) {
            loadedTheme = activityTheme;
            loadTheme(loadedTheme);
        }
    }

    abstract void loadTheme(AppTheme theme);
}
