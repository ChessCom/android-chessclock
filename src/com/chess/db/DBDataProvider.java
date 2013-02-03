package com.chess.db;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.chess.backend.statics.AppData;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataProvider extends ContentProvider {
	private static final UriMatcher uriMatcher;
	private static final UriMatcher uriMatcherIds;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcherIds = new UriMatcher(UriMatcher.NO_MATCH);

		// get table Uri's
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.TACTICS_BATCH_TABLE, DBConstants.TACTICS_BATCH);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.TACTICS_RESULTS_TABLE, DBConstants.TACTICS_RESULTS);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_FINISHED_LIST_GAMES_TABLE, DBConstants.ECHESS_FINISHED_LIST_GAMES);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_CURRENT_LIST_GAMES_TABLE, DBConstants.ECHESS_CURRENT_LIST_GAMES);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_ONLINE_GAMES_TABLE, DBConstants.ECHESS_ONLINE_GAMES);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.FRIENDS_TABLE, DBConstants.FRIENDS);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.ARTICLES_TABLE, DBConstants.ARTICLES);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.ARTICLE_CATEGORIES_TABLE, DBConstants.ARTICLE_CATEGORIES);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.VIDEOS_TABLE, DBConstants.VIDEOS);
		uriMatcher.addURI(DBConstants.PROVIDER_NAME, DBConstants.VIDEO_CATEGORIES_TABLE, DBConstants.VIDEO_CATEGORIES);

		// get particular id's from table
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.TACTICS_BATCH_TABLE + "/#", DBConstants.TACTICS_BATCH);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.TACTICS_RESULTS_TABLE + "/#", DBConstants.TACTICS_RESULTS);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_FINISHED_LIST_GAMES_TABLE + "/#", DBConstants.ECHESS_FINISHED_LIST_GAMES);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_CURRENT_LIST_GAMES_TABLE + "/#", DBConstants.ECHESS_CURRENT_LIST_GAMES);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.ECHESS_ONLINE_GAMES_TABLE + "/#", DBConstants.ECHESS_ONLINE_GAMES);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.FRIENDS_TABLE + "/#", DBConstants.FRIENDS);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.ARTICLES_TABLE + "/#", DBConstants.ARTICLES);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.ARTICLE_CATEGORIES_TABLE + "/#", DBConstants.ARTICLE_CATEGORIES);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.VIDEOS_TABLE + "/#", DBConstants.VIDEOS);
		uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, DBConstants.VIDEO_CATEGORIES_TABLE + "/#", DBConstants.VIDEO_CATEGORIES);

	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		final DatabaseHelper dbHelper = new DatabaseHelper(context);
		appDataBase = dbHelper.getWritableDatabase();

		return (appDataBase != null);
	}


	private static Uri[] uriArray = new Uri[]{
			DBConstants.TACTICS_BATCH_CONTENT_URI,
			DBConstants.TACTICS_RESULTS_CONTENT_URI,
			DBConstants.ECHESS_FINISHED_LIST_GAMES_CONTENT_URI,
			DBConstants.ECHESS_CURRENT_LIST_GAMES_CONTENT_URI,
			DBConstants.ECHESS_ONLINE_GAMES_CONTENT_URI,
			DBConstants.FRIENDS_CONTENT_URI,
			DBConstants.ARTICLES_CONTENT_URI,
			DBConstants.ARTICLE_CATEGORIES_CONTENT_URI,
			DBConstants.VIDEOS_CONTENT_URI,
			DBConstants.VIDEO_CATEGORIES_CONTENT_URI
	};

	private static int[] pathsArray = new int[]{
			DBConstants.TACTICS_BATCH,
			DBConstants.TACTICS_RESULTS,
			DBConstants.ECHESS_FINISHED_LIST_GAMES,
			DBConstants.ECHESS_CURRENT_LIST_GAMES,
			DBConstants.ECHESS_ONLINE_GAMES,
			DBConstants.FRIENDS,
			DBConstants.ARTICLES,
			DBConstants.ARTICLE_CATEGORIES,
			DBConstants.VIDEOS,
			DBConstants.VIDEO_CATEGORIES
	};

	private static String[] tablesArray = new String[]{
			DBConstants.TACTICS_BATCH_TABLE,
			DBConstants.TACTICS_RESULTS_TABLE,
			DBConstants.ECHESS_FINISHED_LIST_GAMES_TABLE,
			DBConstants.ECHESS_CURRENT_LIST_GAMES_TABLE,
			DBConstants.ECHESS_ONLINE_GAMES_TABLE,
			DBConstants.FRIENDS_TABLE,
			DBConstants.ARTICLES_TABLE,
			DBConstants.ARTICLE_CATEGORIES_TABLE,
			DBConstants.VIDEOS_TABLE,
			DBConstants.VIDEO_CATEGORIES_TABLE
	};

	private static String[] createTablesArray = new String[]{
			DBConstants.TACTICS_BATCH_TABLE_CREATE,
			DBConstants.TACTICS_RESULTS_TABLE_CREATE,
			DBConstants.ECHESS_FINISHED_LIST_GAMES_CREATE,
			DBConstants.ECHESS_CURRENT_LIST_GAMES_CREATE,
			DBConstants.ECHESS_ONLINE_GAMES_CREATE,
			DBConstants.FRIENDS_CREATE,
			DBConstants.ARTICLES_CREATE,
			DBConstants.ARTICLE_CATEGORIES_CREATE,
			DBConstants.VIDEOS_CREATE,
			DBConstants.VIDEO_CATEGORIES_CREATE
	};

	@Override
	public String getType(Uri uri) {
		for (int element : pathsArray) {
			if (uriMatcher.match(uri) == element) {
				return "vnd.android.cursor.dir/" + DBConstants.PROVIDER_NAME;
			} else if (uriMatcherIds.match(uri) == element) {
				return "vnd.android.cursor.item/" + DBConstants.PROVIDER_NAME;
			}
		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

		boolean found = false;
		for (int i = 0; i < pathsArray.length; i++) {
			if (uriMatcher.match(uri) == pathsArray[i]) {
				sqlBuilder.setTables(tablesArray[i]);
				found = true;
			} else if (uriMatcherIds.match(uri) == pathsArray[i]) {
				sqlBuilder.setTables(tablesArray[i]);
				sqlBuilder.appendWhere(DBConstants._ID + " = " + uri.getPathSegments().get(1));
				found = true;
			}

			if (found) {
				Cursor c = sqlBuilder.query(appDataBase, projection, selection, selectionArgs, null, null, sortOrder);
				c.setNotificationUri(getContext().getContentResolver(), uri);
				return c;
			}
		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count = 0;

		boolean found = false;
		for (int i = 0; i < pathsArray.length; i++) {
			if (uriMatcher.match(uri) == pathsArray[i]) {
				count = appDataBase.update(tablesArray[i], values, selection, selectionArgs);
				found = true;
			} else if (uriMatcherIds.match(uri) == pathsArray[i]) {
				count = appDataBase.update(tablesArray[i], values,
						DBConstants._ID + " = " + uri.getPathSegments().get(1) +
								(!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
						selectionArgs);
				found = true;
			}

			if (found) {
				getContext().getContentResolver().notifyChange(uri, null);
				return count;
			}
		}
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		for (int i = 0; i < pathsArray.length; i++) {
			if (uriMatcher.match(uri) == pathsArray[i] || uriMatcherIds.match(uri) == pathsArray[i]) {
				long rowID = appDataBase.insert(tablesArray[i], "", values);
				//---if added successfully---
				if (rowID > 0) {
					Uri _uri = ContentUris.withAppendedId(uriArray[i], rowID);
					getContext().getContentResolver().notifyChange(_uri, null);
					return _uri;
				}
			}
		}
//		throw new IllegalArgumentException("Unsupported URI: " + uri);

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		boolean found = false;
		for (int i = 0; i < pathsArray.length; i++) {
			if (uriMatcher.match(uri) == pathsArray[i]) {
				count = appDataBase.delete(tablesArray[i], selection, selectionArgs);
				found = true;
			} else if (uriMatcherIds.match(uri) == pathsArray[i]) {
				String id = uri.getPathSegments().get(1);
				count = appDataBase.delete(
						tablesArray[i],
						DBConstants._ID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
						selectionArgs);
				found = true;
			}

			if (found) {
				getContext().getContentResolver().notifyChange(uri, null);
				return count;
			}
		}
		throw new IllegalArgumentException("Unknown URI " + uri);
	}

	/**
	 * Retrieve version of DB to sync data, and exclude null data request from DB
	 *
	 * @return DATABASE_VERSION integer value
	 */
	public static int getDbVersion() {
		return DBConstants.DATABASE_VERSION;
	}


	private SQLiteDatabase appDataBase;

	public SQLiteDatabase getDbHandle() {
		return appDataBase;
	}


	private static class DatabaseHelper extends SQLiteOpenHelper {
		private Context context;
		DatabaseHelper(Context context) {
			super(context, DBConstants.DATABASE_NAME, null, DBConstants.DATABASE_VERSION);
			this.context = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			for (String createTableCall : createTablesArray) {
				db.execSQL(createTableCall);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			AppData.clearPreferences(context); // clear all values, to avoid class cast exceptions

			Log.w("Content provider database",
					"Upgrading database from version " +
							oldVersion + " to " + newVersion +
							", which will destroy all old data");
			// TODO handle backup data
			for (String table :tablesArray) {
				db.execSQL("DROP TABLE IF EXISTS " + table);
			}

			onCreate(db);
		}
	}
}
