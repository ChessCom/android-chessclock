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

	private static String TIME_CONTROLS_PREF_NAME = "timeControls";
	private static String TIME_CONTROL_SELECTED_PREF_IDX = "timeControlIdx";
	private static String TIME_CONTROLS_PREF_FIELD_NAME = "json";

	private static SharedPreferences getSharedPreferences(Context context) {
		// Preferences stored on /data/data/PACKAGE_NAME/shared_prefs/timeControls.xml
		SharedPreferences sp = context.getSharedPreferences(TIME_CONTROLS_PREF_NAME, Context.MODE_PRIVATE);
		return sp;
	}

	/**
	 * Helper to fetch the last used time control and start the clock engine on app launch.
	 *
	 * @param context
	 */
	public static void startClockWithLastTimeControl(Context context) {

		ArrayList<TimeControl> timeControls = restoreTimeControlsList(context);

		// Build default List if none was restored from shared preferences.
		if (timeControls == null || timeControls.size() == 0) {
			Log.i(TAG, "Time controls list empty. Building and saving default list.");
			timeControls = TimeControlParser.buildDefaultTimeControlsList(context);
		}

		int index = getLastTimeControlCheckIndex(context);
		index = Math.max(index, 0);
		index = Math.min(index, timeControls.size() - 1);

		try {
			TimeControl playerOne = timeControls.get(index);
			TimeControl playerTwo = (TimeControl) playerOne.clone();

			Intent startServiceIntent =
					ChessClockLocalService.getChessClockServiceIntent(context, playerOne, playerTwo);
			context.startService(startServiceIntent);

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Store TimeControl Array in shared preferences as json String.
	 *
	 * @param timeControls
	 * @return
	 */
	public static boolean saveTimeControls(Context context, ArrayList<TimeControl> timeControls) {

		if (timeControls == null) {
			Log.w(TAG, "Save time controls requested with empty list. Ignoring request.");
			return false;
		}

		Log.i(TAG, "Saving " + timeControls.size() + " time controls");
		JSONObject json = new JSONObject();
		JSONArray timeControlJSONArray = new JSONArray();
		try {
			for (TimeControl tc : timeControls) {
				JSONObject timeControlJSONObject = new JSONObject();
				JSONArray timeControlStagesJSONArray = new JSONArray();

				// Save Stages
				for (Stage stage : tc.getStageManager().getStages()) {
					JSONObject stageJSONObject = new JSONObject();
					stageJSONObject.put("id", stage.getId());
					stageJSONObject.put("duration", stage.getDuration());
					stageJSONObject.put("moves", stage.getTotalMoves());
					timeControlStagesJSONArray.put(stageJSONObject);
				}

				// Save TimeIncrement
				JSONObject timeIncrementJSONOBject = new JSONObject();
				timeIncrementJSONOBject.put("value", tc.getTimeIncrement().getValue());
				timeIncrementJSONOBject.put("type", tc.getTimeIncrement().getType().getValue());

				// Add name, stages and time increment to TimeControl json object.
				timeControlJSONObject.put("name", tc.getName());
				timeControlJSONObject.put("timeincrement", timeIncrementJSONOBject);
				timeControlJSONObject.put("stages", timeControlStagesJSONArray);

				// Add TimeControl json object to JSONArray
				timeControlJSONArray.put(timeControlJSONObject);
			}

			json.put("timecontrols", timeControlJSONArray);
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		}

		String jsonString = json.toString();
		SharedPreferences sp = getSharedPreferences(context);
		SharedPreferences.Editor spe = sp.edit();
		spe.putString(TIME_CONTROLS_PREF_FIELD_NAME, jsonString);

		// Note: commit method is a synchronous, so it might block.
		return spe.commit();
	}

	/**
	 * Stores the selected time control position in the time control list.
	 *
	 * @param idx list index.
	 * @return true if successfully saved.
	 */
	public static boolean saveTimeControlCheckIndex(Context context, int idx) {
		SharedPreferences sp = getSharedPreferences(context);
		SharedPreferences.Editor spe = sp.edit();
		spe.putInt(TIME_CONTROL_SELECTED_PREF_IDX, idx);
		return spe.commit();
	}


	/**
	 * Get the stored selected time control position in the time control list.
	 *
	 * @return position of the last selected time control in the list.
	 */
	public static int getLastTimeControlCheckIndex(Context context) {
		SharedPreferences sp = getSharedPreferences(context);
		int idx = sp.getInt(TIME_CONTROL_SELECTED_PREF_IDX, 0);
		return Math.max(idx, 0);
	}

	/**
	 * Get the stored TimeControl list from shared preferences.
	 *
	 * @return TimeControl list.
	 */
	public static ArrayList<TimeControl> restoreTimeControlsList(Context context) {

		Log.i(TAG, "Looking for stored time controls");

		SharedPreferences sp = getSharedPreferences(context);
		String jsonString = sp.getString(TIME_CONTROLS_PREF_FIELD_NAME, null);

		if (jsonString == null) {
			Log.w(TAG, "Not able to read the preference");
			return null;
		}

		ArrayList<TimeControl> timeControls = new ArrayList<TimeControl>();
		try {
			JSONObject json = new JSONObject(jsonString);
			JSONArray timeControlsJSONArray = json.getJSONArray("timecontrols");
			for (int i = 0; i < timeControlsJSONArray.length(); i++) {
				JSONObject timeControlJSON = timeControlsJSONArray.getJSONObject(i);

				JSONObject timeIncrementJSONOBject = timeControlJSON.getJSONObject("timeincrement");
				JSONArray timeControlStagesJSON = timeControlJSON.getJSONArray("stages");

				String name = timeControlJSON.getString("name");
				Stage[] stages = getStages(timeControlStagesJSON);
				TimeIncrement timeIncrement = getTimeIncrement(timeIncrementJSONOBject);

				TimeControl timeControl = new TimeControl(name, stages, timeIncrement);
				timeControls.add(timeControl);
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
	 * Creates default TimeControl list, saves it on shared preferences.
	 *
	 * @return Default TimeControl list.
	 */
	public static ArrayList<TimeControl> buildDefaultTimeControlsList(Context context) {
		Log.i(TAG, "Building and saving default time control list");

		ArrayList<TimeControl> timeControls = new ArrayList<TimeControl>();

		// Fischer blitz 5|0
		Stage fischerBlitzStage = new Stage(0, 300000);
		TimeIncrement fischerBlitzTI = new TimeIncrement(TimeIncrement.Type.FISCHER, 0);
		TimeControl fischerBlitzTC = new TimeControl("Fischer Blitz 5|0", new Stage[]{fischerBlitzStage}, fischerBlitzTI);

		// Delay bullet 1|2
		Stage delayBulletStage = new Stage(0, 60000);
		TimeIncrement delayBulletTimeIncrement = new TimeIncrement(TimeIncrement.Type.DELAY, 2000);
		TimeControl delayBulletTimeControl = new TimeControl("Delay Bullet 1|2", new Stage[]{delayBulletStage}, delayBulletTimeIncrement);

		// Blitz 5|5
		Stage blitz55 = new Stage(0, 300000);
		TimeIncrement blitzTimeIncrement = new TimeIncrement(TimeIncrement.Type.FISCHER, 5000);
		TimeControl blitzTimeControl = new TimeControl("Fischer 5|5", new Stage[]{blitz55}, blitzTimeIncrement);

		// Fischer rapid 10|5
		Stage fischerRapidStage = new Stage(0, 600000);
		TimeIncrement fischerRapidTI = new TimeIncrement(TimeIncrement.Type.FISCHER, 5000);
		TimeControl fischerRapid = new TimeControl("Fischer rapid 10|5", new Stage[]{fischerRapidStage}, fischerRapidTI);

		// Tournament
		Stage tournamentStage1 = new Stage(0, 7200000, 40);
		Stage tournamentStage2 = new Stage(1, 3600000);
		TimeIncrement tournamentTI = new TimeIncrement(TimeIncrement.Type.DELAY, 5000);
		TimeControl tournamentTC = new TimeControl("Tournament 40/2hr, G60, 5s delay", new Stage[]{tournamentStage1, tournamentStage2}, tournamentTI);

		timeControls.add(fischerBlitzTC);
		timeControls.add(delayBulletTimeControl);
		timeControls.add(blitzTimeControl);
		timeControls.add(fischerRapid);
		timeControls.add(tournamentTC);

		// Saving default time controls
		TimeControlParser.saveTimeControls(context, timeControls);

		return timeControls;
	}

	private static TimeIncrement getTimeIncrement(JSONObject timeIncrementJSONObject) {
		try {
			// Restore TimeIncrement
			long value = timeIncrementJSONObject.getLong("value");
			TimeIncrement.Type type
					= TimeIncrement.Type.fromInteger(timeIncrementJSONObject.getInt("type"));

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
			int id = stageJSONObject.getInt("id");
			long duration = stageJSONObject.getLong("duration");
			int moves = stageJSONObject.getInt("moves");
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
