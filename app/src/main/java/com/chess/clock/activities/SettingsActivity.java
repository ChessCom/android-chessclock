package com.chess.clock.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.chess.clock.R;
import com.chess.clock.engine.TimeControl;
import com.chess.clock.engine.TimeControlManager;
import com.chess.clock.fragments.SettingsFragment;
import com.chess.clock.fragments.TimeControlFragment;
import com.chess.clock.service.ChessClockLocalService;
import com.chess.clock.statics.AppData;

import java.util.ArrayList;

/**
 * Activity that manages TimeControl list in the Settings and also TimeControl form.
 */
public class SettingsActivity extends ActionBarActivity implements SettingsFragment.OnSettingsListener, TimeControlFragment.OnTimeControlListener, TimeControlManager.Callback {

    private static final String TAG = SettingsActivity.class.getName();

    /**
     * Fragments TAG
     */
    private final String TAG_SETTINGS_FRAGMENT = "settings";
    private final String TAG_TIME_CONTROL_FRAGMENT = "time_control";

    /**
     * Shared preferences wrapper
     */
    private AppData appData;

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
    private ServiceConnection mConnection = new ServiceConnection() {

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
    private TimeControlManager mTimeControlManager;

    /**
     * BottomNavigationTab
     */
    BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        appData = new AppData(getApplicationContext());

        // This must be called before super.onCreate which performs initialization of all fragments
        // and loaders. TimeControl objects initialization is required before that.
        mTimeControlManager = new TimeControlManager(getApplicationContext(), savedInstanceState);
        mTimeControlManager.setTimeControlManagerListener(this);

        // Perform initialization of all fragments and loaders.
        super.onCreate(savedInstanceState);

        boolean isFullScreen = appData.getClockFullScreen();
        if (isFullScreen) {
            showFullScreen();
        } else {
            hideFullScreen();
        }

        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment(), TAG_SETTINGS_FRAGMENT)
                    .commit();
        }

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.player_selection_bottom_navigation);
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
        mTimeControlManager.saveTimeControlIndex(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        Fragment frag = getSupportFragmentManager().findFragmentByTag(TAG_TIME_CONTROL_FRAGMENT);
        if (frag != null && frag.isVisible()) {
            ((TimeControlFragment) frag).showConfirmGoBackDialog();
        } else {
            finish();
            overridePendingTransition(R.anim.left_to_right_in, R.anim.left_to_right_full);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mTimeControlManager.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void showFullScreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);

    }

    public void hideFullScreen() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
    }

    /**
     * @return True if Time Control set up in Clock Timers Activity is the same
     */
    public boolean isSameTimeControlLoaded() {
        int index = mTimeControlManager.getEditableTimeControlCheckIndex();
        if (index > 0 && index < mTimeControlManager.getTimeControls().size()) {
            TimeControl tc = mTimeControlManager.getTimeControls().get(index);
            String title = tc.getName();
            return mBound && mService.getNameOfTimeControlRunning().equals(title);
        } else {
            Log.e(TAG, "isSameTimeControlLoaded got index out of bounds. index: "
                    + index + " array size: " + mTimeControlManager.getTimeControls().size());
            return false;
        }

    }

    /**
     * FRAGMENT TRANSACTIONS
     */

    public void loadTimeControlFragment() {
        loadFragment(new TimeControlFragment(), TAG_TIME_CONTROL_FRAGMENT);
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
    public ArrayList<TimeControl> getCurrentTimeControls() {
        return mTimeControlManager.getTimeControls();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @return Current checked position of TimeControl in the list.
     */
    @Override
    public int getCheckedTimeControlIndex() {
        return mTimeControlManager.getEditableTimeControlCheckIndex();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param idx time control list position.
     */
    @Override
    public void setCheckedTimeControlIndex(int idx) {
        mTimeControlManager.setEditableTimeControlCheckIndex(idx);
    }

    /**
     * Used as communication gateway by SettingsFragment.
     */
    @Override
    public void addTimeControl() {

        mTimeControlManager.prepareNewEditableTimeControl();

        // Load UI
        loadTimeControlFragment();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     *
     * @param position TimeControl position in the list.
     */
    @Override
    public void loadTimeControl(int position) {
        mTimeControlManager.prepareEditableTimeControl(position);
        loadTimeControlFragment();
    }

    /**
     * Used as communication gateway by SettingsFragment.
     */
    @Override
    public void removeTimeControl(int[] positions) {
        mTimeControlManager.removeTimeControls(getApplicationContext(), positions);
    }

    /**
     * Used as communication gateway by TimeControlFragment.
     *
     * @return Current TimeControl being used.
     */
    @Override
    public TimeControl getEditableTimeControl() {
        return mTimeControlManager.getEditableTimeControl();
    }

    /**
     * Used as communication gateway by TimeControlFragment.
     */
    @Override
    public void saveTimeControl() {
        mTimeControlManager.saveTimeControl(getApplicationContext());
        SettingsFragment f = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(TAG_SETTINGS_FRAGMENT);
        if (f != null) {
            f.refreshTimeControlList();
        }
    }

    @Override
    public void onTimeControlListEmpty() {
        Toast.makeText(this, getString(R.string.list_empty_toast_message), Toast.LENGTH_LONG).show();
    }

    public void setNavigationOnItemSelectedListener(OnNavigationItemSelectedListener listener) {
        if (mBottomNavigationView != null) {
            mBottomNavigationView.setOnNavigationItemSelectedListener(listener);
        }
    }

    public void setBottomNavigationViewVisibility(int v) {
        mBottomNavigationView.setVisibility(v);
    }
}
