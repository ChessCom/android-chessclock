package com.chess.clock.engine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.chess.clock.service.ChessClockLocalService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Helper Class to save/restore TimeControl objects on Shared Preferences using Json Strings.
 */
public class TimeControlParser {

    private static final String TAG = TimeControlParser.class.getName();

    private static final String TIME_CONTROLS_PREF_NAME = "timeControls";
    private static final String TIME_CONTROL_SELECTED_PREF_IDX = "timeControlIdx";
    private static final String TIME_CONTROLS_PREF_FIELD_NAME = "json";
    private static final String TC_JSON_ID = "id";
    private static final String TC_JSON_DURATION = "duration";
    private static final String TC_JSON_MOVES = "moves";
    private static final String TC_JSON_VALUE = "value";
    private static final String TC_JSON_TYPE = "type";
    private static final String TC_JSON_NAME = "name";
    private static final String TC_JSON_TIME_INCREMENT = "time_increment";
    private static final String TC_JSON_TIME_INCREMENT_PLAYER_TWO = "time_increment_player_two";
    private static final String TC_JSON_STAGES = "stages";
    private static final String TC_JSON_STAGES_PLAYER_TWO = "stages_player_two";
    private static final String TC_JSON_TIME_CONTROLS = "time_controls";
    private static final String TC_JSON_SAME_AS_PLAYER_ONE = "same_as_player_one";

    private static SharedPreferences getSharedPreferences(Context context) {
        // Preferences stored on /data/data/PACKAGE_NAME/shared_prefs/timeControls.xml
        return context.getSharedPreferences(TIME_CONTROLS_PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Helper to fetch the last used time control and start the clock engine on app launch.
     */
    public static void startClockWithLastTimeControl(Context context) {

        ArrayList<TimeControlWrapper> timeControls = restoreTimeControlsList(context);

        // Build default List if none was restored from shared preferences.
        if (timeControls == null || timeControls.size() == 0) {
            Log.i(TAG, "Time controls list empty. Building and saving default list.");
            timeControls = TimeControlDefaults.buildDefaultTimeControlsList(context);
        }

        int index = getLastTimeControlCheckIndex(context);
        index = Math.max(index, 0);
        index = Math.min(index, timeControls.size() - 1);

        TimeControl playerOne = timeControls.get(index).getTimeControlPlayerOne();
        TimeControl playerTwo = timeControls.get(index).getTimeControlPlayerTwo();

        Intent startServiceIntent =
                ChessClockLocalService.getChessClockServiceIntent(context, playerOne, playerTwo);
        context.startService(startServiceIntent);
    }

    /**
     * Store TimeControl Array in shared preferences as json String.
     */
    public static void saveTimeControls(Context context, ArrayList<TimeControlWrapper> timeControls) {

        if (timeControls == null) {
            Log.w(TAG, "Save time controls requested with empty list. Ignoring request.");
            return;
        }

        Log.i(TAG, "Saving " + timeControls.size() + " time controls");
        JSONObject json = new JSONObject();
        JSONArray timeControlJSONArray = new JSONArray();
        try {
            for (TimeControlWrapper tc : timeControls) {
                JSONObject timeControlJSONObject = new JSONObject();
                JSONArray timeControlStagesJSONArray = new JSONArray();
                JSONArray timeControlStagesPlayerTwoJSONArray = new JSONArray();

                // Save Stages
                for (Stage stage : tc.getTimeControlPlayerOne().getStageManager().getStages()) {
                    JSONObject stageJSONObject = new JSONObject();
                    stageJSONObject.put(TC_JSON_ID, stage.getId());
                    stageJSONObject.put(TC_JSON_DURATION, stage.getDuration());
                    stageJSONObject.put(TC_JSON_MOVES, stage.getTotalMoves());
                    timeControlStagesJSONArray.put(stageJSONObject);
                }

                for (Stage stage : tc.getTimeControlPlayerTwo().getStageManager().getStages()) {
                    JSONObject stageJSONObject = new JSONObject();
                    stageJSONObject.put(TC_JSON_ID, stage.getId());
                    stageJSONObject.put(TC_JSON_DURATION, stage.getDuration());
                    stageJSONObject.put(TC_JSON_MOVES, stage.getTotalMoves());
                    timeControlStagesPlayerTwoJSONArray.put(stageJSONObject);
                }

                // Save TimeIncrement
                JSONObject timeIncrementJSONObject = new JSONObject();
                timeIncrementJSONObject.put(TC_JSON_VALUE, tc.getTimeControlPlayerOne().getTimeIncrement().getValue());
                timeIncrementJSONObject.put(TC_JSON_TYPE, tc.getTimeControlPlayerOne().getTimeIncrement().getType().getValue());

                JSONObject timeIncrementPlayerTwoJSONObject = new JSONObject();
                timeIncrementPlayerTwoJSONObject.put(TC_JSON_VALUE, tc.getTimeControlPlayerTwo().getTimeIncrement().getValue());
                timeIncrementPlayerTwoJSONObject.put(TC_JSON_TYPE, tc.getTimeControlPlayerTwo().getTimeIncrement().getType().getValue());

                // Add name, stages and time increment to TimeControl json object.
                timeControlJSONObject.put(TC_JSON_NAME, tc.getTimeControlPlayerOne().getName());
                timeControlJSONObject.put(TC_JSON_TIME_INCREMENT, timeIncrementJSONObject);
                timeControlJSONObject.put(TC_JSON_TIME_INCREMENT_PLAYER_TWO, timeIncrementPlayerTwoJSONObject);
                timeControlJSONObject.put(TC_JSON_STAGES, timeControlStagesJSONArray);
                timeControlJSONObject.put(TC_JSON_STAGES_PLAYER_TWO, timeControlStagesPlayerTwoJSONArray);

                // Add same as player one boolean
                timeControlJSONObject.put(TC_JSON_SAME_AS_PLAYER_ONE, tc.isSameAsPlayerOne());

                // Add TimeControl json object to JSONArray
                timeControlJSONArray.put(timeControlJSONObject);
            }

            json.put(TC_JSON_TIME_CONTROLS, timeControlJSONArray);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return;
        }

        String jsonString = json.toString();
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString(TIME_CONTROLS_PREF_FIELD_NAME, jsonString);

        // Note: commit method is a synchronous, so it might block.
        spe.commit();
    }

    /**
     * Stores the selected time control position in the time control list.
     *
     * @param idx list index.
     */
    public static void saveTimeControlCheckIndex(Context context, int idx) {
        SharedPreferences sp = getSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(TIME_CONTROL_SELECTED_PREF_IDX, idx);
        spe.commit();
    }


    /**
     * Get the stored selected time control position in the time control list.
     *
     * @return position of the last selected time control in the list.
     */
    public static int getLastTimeControlCheckIndex(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        int idx = sp.getInt(TIME_CONTROL_SELECTED_PREF_IDX, TimeControlDefaults.DEFAULT_TIME_INDEX);
        return Math.max(idx, 0);
    }

    /**
     * Get the stored TimeControl list from shared preferences.
     *
     * @return TimeControl list.
     */
    public static ArrayList<TimeControlWrapper> restoreTimeControlsList(Context context) {

        Log.i(TAG, "Looking for stored time controls");

        SharedPreferences sp = getSharedPreferences(context);
        String jsonString = sp.getString(TIME_CONTROLS_PREF_FIELD_NAME, null);

        if (jsonString == null) {
            Log.w(TAG, "Not able to read the preference");
            return null;
        }
        jsonString = checkJsonStringAndMigrateIfNeeded(sp, jsonString);

        ArrayList<TimeControlWrapper> timeControls = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(jsonString);
            JSONArray timeControlsJSONArray = json.getJSONArray(TC_JSON_TIME_CONTROLS);
            for (int i = 0; i < timeControlsJSONArray.length(); i++) {
                JSONObject timeControlJSON = timeControlsJSONArray.getJSONObject(i);
                JSONObject timeIncrementPlayerTwoJSONOBject = null;
                JSONArray timeControlStagesPlayerTwoJSON = null;

                JSONObject timeIncrementJSONOBject = timeControlJSON.getJSONObject(TC_JSON_TIME_INCREMENT);
                JSONArray timeControlStagesJSON = timeControlJSON.getJSONArray(TC_JSON_STAGES);

                if (timeControlJSON.has(TC_JSON_TIME_INCREMENT_PLAYER_TWO) && timeControlJSON.has(TC_JSON_STAGES_PLAYER_TWO)) {
                    timeIncrementPlayerTwoJSONOBject = timeControlJSON.getJSONObject(TC_JSON_TIME_INCREMENT_PLAYER_TWO);
                    timeControlStagesPlayerTwoJSON = timeControlJSON.getJSONArray(TC_JSON_STAGES_PLAYER_TWO);
                }

                String name = timeControlJSON.getString(TC_JSON_NAME);
                Stage[] stages = getStages(timeControlStagesJSON);
                Stage[] stagesPlayerTwo = timeControlStagesPlayerTwoJSON == null ? stages : getStages(timeControlStagesPlayerTwoJSON);
                TimeIncrement timeIncrement = getTimeIncrement(timeIncrementJSONOBject);
                TimeIncrement timeIncrementPlayerTwo = timeIncrementPlayerTwoJSONOBject == null ? timeIncrement : getTimeIncrement(timeIncrementPlayerTwoJSONOBject);

                boolean isSameAsPlayerOne = !timeControlJSON.has(TC_JSON_SAME_AS_PLAYER_ONE) ||
                        timeControlJSON.getBoolean(TC_JSON_SAME_AS_PLAYER_ONE);

                TimeControl timeControl = new TimeControl(name, stages, timeIncrement);
                TimeControl timeControlPlayerTwo = new TimeControl(name, stagesPlayerTwo, timeIncrementPlayerTwo);
                TimeControlWrapper wrapper = new TimeControlWrapper(timeControl, timeControlPlayerTwo);
                wrapper.setSameAsPlayerOne(isSameAsPlayerOne);
                timeControls.add(wrapper);
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }

        Log.i(TAG, "Retrieving " + timeControls.size() + " time controls.");
        return timeControls;
    }

    /**
     * migrate json data format to new if needed
     */
    private static String checkJsonStringAndMigrateIfNeeded(SharedPreferences sp, String jsonString) {
        boolean userHasOldVersion = jsonString.contains("timecontrols");
        if (userHasOldVersion) {
            String newJsonString = jsonString.replace("timecontrols", TC_JSON_TIME_CONTROLS)
                    .replace("timeincrement", TC_JSON_TIME_INCREMENT);
            sp.edit().putString(TIME_CONTROLS_PREF_FIELD_NAME, newJsonString).apply();
            jsonString = newJsonString;
        }
        return jsonString;
    }

    private static TimeIncrement getTimeIncrement(JSONObject timeIncrementJSONObject) {
        try {
            // Restore TimeIncrement
            long value = timeIncrementJSONObject.getLong(TC_JSON_VALUE);
            TimeIncrement.Type type
                    = TimeIncrement.Type.fromInteger(timeIncrementJSONObject.getInt(TC_JSON_TYPE));

            return new TimeIncrement(type, value);

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Stage[] getStages(JSONArray stagesJSONArray) {
        try {
            Stage[] stages = new Stage[stagesJSONArray.length()];
            for (int i = 0; i < stagesJSONArray.length(); i++) {
                JSONObject stageJSONObject = stagesJSONArray.getJSONObject(i);
                Stage stage = getStage(stageJSONObject);
                stages[i] = stage;
            }
            return stages;
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Stage getStage(JSONObject stageJSONObject) {
        try {
            int id = stageJSONObject.getInt(TC_JSON_ID);
            long duration = stageJSONObject.getLong(TC_JSON_DURATION);
            int moves = stageJSONObject.getInt(TC_JSON_MOVES);
            if (moves > 0) {
                return new Stage(id, duration, moves);
            } else {
                return new Stage(id, duration);
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
