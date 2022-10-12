package com.chess.clock.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlManager;
import com.chess.clock.engine.TimeControlWrapper;
import com.chess.clock.fragments.TimeControlFragment;
import com.chess.clock.fragments.TimeSettingsFragment;
import com.chess.clock.service.ChessClockLocalService;

import java.util.ArrayList;

/**
 * Activity that manages TimeControl list in the Settings and also TimeControl form.
 */
public class TimerSettingsActivity extends BaseActivity implements TimeSettingsFragment.OnSettingsListener, TimeControlFragment.OnTimeControlListener, TimeControlManager.Callback {

    private static final String TAG = TimerSettingsActivity.class.getName();

    /**
     * Fragments TAG
     */
    private final String TAG_SETTINGS_FRAGMENT = "settings";
    private final String TAG_TIME_CONTROL_FRAGMENT = "time_control";

    /**
     * Chess clock local service (clock engine).
     */
    ChessClockLocalService mService;

    /**
     * True when this activity is bound to chess clock service.
     */
    boolean mBound = false;

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ChessClockLocalService.ChessClockLocalServiceBinder binder
                    = (ChessClockLocalService.ChessClockLocalServiceBinder) service;
            mService = binder.getService();
            mBound = true;

            Log.i(TAG, "Service bound connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            Log.i(TAG, "Service bound disconnected");
        }
    };

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

        boolean isFullScreen = appData.getClockFullScreen();
        if (isFullScreen) {
            showStatusBar();
        } else {
            hideStatusBar();
        }

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

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to Local Chess clock Service.
        Intent intent = new Intent(this, ChessClockLocalService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Log.i(TAG, "Binding UI to Chess Clock Service.");
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the chess clock service.
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            Log.i(TAG, "Unbinding UI from Chess Clock Service.");
        }
    }

    public void dismiss() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // store last settings time control list check position on shared preferences.
        timeControlManager.saveTimeControlIndex(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        showPopupOrFinish(null);
    }

    private void showPopupOrFinish(Integer resultToSet) {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(TAG_TIME_CONTROL_FRAGMENT);
        if (frag != null && frag.isVisible()) {
            ((TimeControlFragment) frag).showConfirmGoBackDialog();
        } else {
            if (resultToSet != null) {
                setResult(resultToSet);
            }
            finish();
            overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
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
            showPopupOrFinish(RESULT_CANCELED);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @return True if Time Control set up in Clock Timers Activity is the same
     */
    public boolean isSameTimeControlLoaded() {
        int index = timeControlManager.getEditableTimeControlCheckIndex();
        if (index > 0 && index < timeControlManager.getTimeControls().size()) {
            TimeControl tc = timeControlManager.getTimeControls().get(index).getTimeControlPlayerOne();
            String title = tc.getName();
            return mBound && mService.getNameOfTimeControlRunning().equals(title);
        } else {
            Log.e(TAG, "isSameTimeControlLoaded got index out of bounds. index: "
                    + index + " array size: " + timeControlManager.getTimeControls().size());
            return false;
        }
    }

    /**
     * FRAGMENT TRANSACTIONS
     */

    public void loadTimeControlFragment(boolean edit) {
        loadFragment(TimeControlFragment.newInstance(edit), TAG_TIME_CONTROL_FRAGMENT);
    }

    private void loadFragment(Fragment fragment, String tag) {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, tag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
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
    public int getCheckedTimeControlIndex() {
        return timeControlManager.getEditableTimeControlCheckIndex();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param idx time control list position.
     */
    @Override
    public void setCheckedTimeControlIndex(int idx) {
        timeControlManager.setEditableTimeControlCheckIndex(idx);
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
     * @param position TimeControl position in the list.
     */
    @Override
    public void loadTimeControl(int position) {
        timeControlManager.prepareEditableTimeControl(position);
        loadTimeControlFragment(true);
    }

    /**
     * Used as communication gateway by SettingsFragment.
     */
    @Override
    public void removeTimeControl(int[] positions) {
        timeControlManager.removeTimeControls(getApplicationContext(), positions);
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
        TimeSettingsFragment f = (TimeSettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        if (f != null) {
            f.refreshTimeControlList();
        }
    }

    @Override
    public void onTimeControlListEmpty() {
        Toast.makeText(this, getString(R.string.list_empty_toast_message), Toast.LENGTH_LONG).show();
    }
}
