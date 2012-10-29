package com.chess.db;

import android.content.ContentValues;
import android.database.Cursor;
import com.chess.model.TacticItem;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataManager {
	private final static String TAG = "DBDataManager";

    private static final String ORDER_BY = "ORDER BY";
    private static final String GROUP_BY = "GROUP BY";
    public static final String OPERATOR_OR = " OR ";
    public static final String OPERATOR_LIKE = " LIKE ";
    public static final String OPERATOR_AND = " AND ";
    public static final String OPERATOR_MORE = " > ";
    public static final String OPERATOR_EQUALS = " = ";

    public static String SELECTION_ITEM_ID;
    public static String SELECTION_TACTIC_BATCH_USER;
    public static String SELECTION_ID;

    public static final String[] PROJECTION_ID = new String[] {
            DBConstants._ID
    };

	public static final String[] PROJECTION_TACTIC_ITEM_ID = new String[] {
			DBConstants._ID,
			DBConstants.V_TACTIC_ID
	};

	public static final String[] PROJECTION_TACTIC_BATCH_USER = new String[] {
			DBConstants._ID,
			DBConstants.V_TACTIC_ID
	};

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants._ID);
		selection.append("=?");
		SELECTION_ID = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_TACTIC_ID);
		selection.append("=?");
		SELECTION_ITEM_ID = selection.toString();
	}

	static {
		StringBuilder selection = new StringBuilder();
		selection.append(DBConstants.V_USER);
		selection.append("=?");
		SELECTION_TACTIC_BATCH_USER = selection.toString();
	}

	public static ContentValues putTacticItemToValues(TacticItem dataObj) {
        ContentValues values = new ContentValues();

		values.put(DBConstants.V_USER, dataObj.getUser());
		values.put(DBConstants.V_TACTIC_ID, dataObj.getId());
        values.put(DBConstants.V_FEN, dataObj.getFen());
        values.put(DBConstants.V_MOVE_LIST, dataObj.getMoveList());
        values.put(DBConstants.V_ATTEMPT_CNT, dataObj.getAttemptCnt());
        values.put(DBConstants.V_PASSED_CNT, dataObj.getPassedCnt());
        values.put(DBConstants.V_RATING, dataObj.getRating());
        values.put(DBConstants.V_AVG_SECONDS, dataObj.getAvgSeconds());

        return values;
    }


	public static TacticItem getTacticItemFromCursor(Cursor cursor) {
		TacticItem dataObj = new TacticItem();

		dataObj.setUser(getString(cursor, DBConstants.V_USER));
		dataObj.setId(getString(cursor, DBConstants.V_TACTIC_ID));
		dataObj.setFen(getString(cursor, DBConstants.V_FEN));
		dataObj.setMoveList(getString(cursor, DBConstants.V_MOVE_LIST));
		dataObj.setAttemptCnt(getString(cursor, DBConstants.V_ATTEMPT_CNT));
		dataObj.setPassedCnt(getString(cursor, DBConstants.V_PASSED_CNT));
		dataObj.setRating(getString(cursor, DBConstants.V_RATING));
		dataObj.setAvgSeconds(getString(cursor, DBConstants.V_AVG_SECONDS));

		return dataObj;
	}


    public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}

	public static int getInt(Cursor cursor, String column) {
		return cursor.getInt(cursor.getColumnIndex(column));
	}

	public static long getLong(Cursor cursor, String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}

	public static long getId(Cursor cursor) {
		return cursor.getLong(cursor.getColumnIndex(DBConstants._ID));
	}
	
	public static int getDbVersion() {
		return DBConstants.DATABASE_VERSION;
	}
}
