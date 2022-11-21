package com.chess.clock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControlManager;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.fragments.TimeControlFragment;
import com.chess.clock.fragments.TimeSettingsFragment;

import java.util.ArrayList;
import java.util.Set;

/**
 * Activity that manages TimeControl list in the Settings and also TimeControl form.
 */
public class TimerSettingsActivity extends TimerServiceActivity implements TimeSettingsFragment.OnSettingsListener, TimeControlFragment.OnTimeControlListener, TimeControlManager.Callback {
    /**
     * Fragments TAG
     */
    private final String TAG_SETTINGS_FRAGMENT = "settings";
    private final String TAG_TIME_CONTROL_FRAGMENT = "time_control";

    /**
     * State
     */
    private TimeControlManager timeControlManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // This must be called before super.onCreate which performs initialization of all fragments
        // and loaders. TimeControl objects initialization is required before that.
        timeControlManager = new TimeControlManager(getApplicationContext(), savedInstanceState);
        timeControlManager.setTimeControlManagerListener(this);

        // Perform initialization of all fragments and loaders.
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new TimeSettingsFragment(), TAG_SETTINGS_FRAGMENT)
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void dismiss() {
        setResult(RESULT_CANCELED);
        finishWithAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // store last settings time control list check position on shared preferences.
        timeControlManager.saveSelectedTimeControlId(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        showPopupOrFinish(null);
    }

    @Override
    void bindUiOnServiceConnected() {
        // no-op
    }

    private void showPopupOrFinish(Integer resultToSet) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(TAG_TIME_CONTROL_FRAGMENT);
        if (frag != null && frag.isVisible()) {
            ((TimeControlFragment) frag).showConfirmGoBackDialog();
        } else {
            if (resultToSet != null) {
                setResult(resultToSet);
            }
            finishWithAnimation();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        timeControlManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            hideKeyboard();
            showPopupOrFinish(RESULT_CANCELED);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        final InputMethodManager inputManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (inputManager != null && currentFocus != null) {
            inputManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * @return True if clock was started before settings changes
     */
    public boolean showResetWarning() {
        return serviceBound && clockService.isClockStarted();
    }

    /**
     * FRAGMENT TRANSACTIONS
     */

    private void loadTimeControlFragment(boolean edit) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, TimeControlFragment.newInstance(edit), TAG_TIME_CONTROL_FRAGMENT)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Used as communication gateway by main SettingsFragment.
     *
     * @return Current TimeControl list being used.
     */
    @Override
    public ArrayList<TimeControlWrapper> getCurrentTimeControls() {
        return timeControlManager.getTimeControls();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @return Current checked position of TimeControl in the list.
     */
    @Override
    public long getCheckedTimeControlId() {
        return timeControlManager.getSelectedTimeControlId();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param id time control wrapper id.
     */
    @Override
    public void setCheckedTimeControlId(long id) {
        timeControlManager.setSelectedTimeControlId(id);
    }

    /**
     * Used as communication gateway by SettingsFragment.
     */
    @Override
    public void addTimeControl() {

        timeControlManager.prepareNewEditableTimeControl();

        // Load UI
        loadTimeControlFragment(false);
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param wrapper TimeControlWrapper to edit.
     */
    @Override
    public void loadTimeControl(TimeControlWrapper wrapper) {
        timeControlManager.prepareEditableTimeControl(wrapper);
        loadTimeControlFragment(true);
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param ids ids of controls to remove
     */
    @Override
    public void removeTimeControl(Set<Long> ids) {
        timeControlManager.removeTimeControls(getApplicationContext(), ids);
    }

    @Override
    public void upDateOrderOnItemMove(int from, int to) {
        timeControlManager.updateOrderOnItemMove(from, to, this);
    }

    @Override
    public void restoreDefaultTimeControls() {
        timeControlManager.restoreDefaultTimeControls(this);
    }

    /**
     * Used as communication gateway by TimeControlFragment.
     *
     * @return Current TimeControl being used.
     */
    @Override
    public TimeControlWrapper getEditableTimeControl() {
        return timeControlManager.getEditableTimeControl();
    }

    /**
     * Used as communication gateway by TimeControlFragment.
     */
    @Override
    public void saveTimeControl() {
        timeControlManager.saveTimeControl(getApplicationContext());
        refreshTimeControlsList();
    }

    private void refreshTimeControlsList() {
        TimeSettingsFragment f = (TimeSettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        if (f != null) {
            f.refreshTimeControlList();
        }
    }

    @Override
    public void onEmptyTimeControlsListRestored() {
        Toast.makeText(this, getString(R.string.list_empty_toast_message), Toast.LENGTH_LONG).show();
        refreshTimeControlsList();
    }
}
