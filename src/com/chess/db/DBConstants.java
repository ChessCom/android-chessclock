package com.chess.db;

import android.net.Uri;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBConstants {

    static final int DATABASE_VERSION 	= 1;  // change version on every DB scheme changes


	public static final String PROVIDER_NAME = "com.chess.db_provider";
	/*
	 * DB table names
	 */
    static final String DATABASE_NAME  = "Chess DB";
    public static final String TACTICS_BATCH_TABLE = "tactics_batch";



	// Content URI
    public static final Uri TACTICS_BATCH_CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/" + TACTICS_BATCH_TABLE);


    // uri paths
    public static final int TACTICS_BATCH = 0;


    // general fields
    public static final String _ID = "_id";
    public static final String _COUNT = "_count";

    /* TacticsItem Fields */

    public static final String V_USER     		= "user";
    public static final String V_TACTIC_ID 		= "tactic_id";
    public static final String V_FEN      		= "fen";
    public static final String V_MOVE_LIST      = "moveList";
    public static final String V_ATTEMPT_CNT    = "attemptCnt";
    public static final String V_PASSED_CNT     = "passedCnt";
    public static final String V_RATING       	= "rating";
    public static final String V_AVG_SECONDS 	= "avgSeconds";

    /* common commands */
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "create table if not exists ";
    private static final String _INT_NOT_NULL 		= " INT not null";
    private static final String _LONG_NOT_NULL 		= " LONG not null";
    private static final String _DOUBLE_NOT_NULL 	= " DOUBLE not null";
    private static final String _TEXT_NOT_NULL 		= " TEXT not null";
    private static final String _COMMA 				= ",";
    private static final String _CLOSE 				= ");";
    private static final String ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT = " (_id integer primary key autoincrement, ";


    static final String TACTICS_BATCH_TABLE_CREATE =
            CREATE_TABLE_IF_NOT_EXISTS + TACTICS_BATCH_TABLE + ID_INTEGER_PRIMARY_KEY_AUTOINCREMENT
			+ V_USER 		+ _TEXT_NOT_NULL + _COMMA
			+ V_TACTIC_ID   + _TEXT_NOT_NULL + _COMMA
			+ V_FEN 		+ _TEXT_NOT_NULL + _COMMA
			+ V_MOVE_LIST 	+ _TEXT_NOT_NULL + _COMMA
			+ V_ATTEMPT_CNT + _TEXT_NOT_NULL + _COMMA
			+ V_PASSED_CNT 	+ _TEXT_NOT_NULL + _COMMA
			+ V_RATING 		+ _TEXT_NOT_NULL + _COMMA
			+ V_AVG_SECONDS + _TEXT_NOT_NULL + _CLOSE;
}
