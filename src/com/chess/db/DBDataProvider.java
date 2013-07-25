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

import static com.chess.db.DBConstants.Tables;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class DBDataProvider extends ContentProvider {

	private static final UriMatcher uriMatcher;
	private static final UriMatcher uriMatcherIds;

	public static final String SLASH_NUMBER = "/#";
	public static final String EQUALS = " = ";

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcherIds = new UriMatcher(UriMatcher.NO_MATCH);

		for (int i = 0; i < Tables.values().length; i++) {
			String table = Tables.values()[i].name();
			uriMatcher.addURI(DBConstants.PROVIDER_NAME, table, i);
		}

		for (int i = 0; i < Tables.values().length; i++) {
			String table = Tables.values()[i].name();
			uriMatcherIds.addURI(DBConstants.PROVIDER_NAME, table + SLASH_NUMBER, i);
		}
	}

	public static final String VND_ANDROID_CURSOR_DIR = "vnd.android.cursor.dir/";
	public static final String VND_ANDROID_CURSOR_ITEM = "vnd.android.cursor.item/";

	@Override
	public boolean onCreate() {
		Context context = getContext();
		final DatabaseHelper dbHelper = new DatabaseHelper(context);
		appDataBase = dbHelper.getWritableDatabase();

		return (appDataBase != null);
	}



	@Override
	public String getType(Uri uri) {
		for (int i=0; i < Tables.values().length; i++) {
			if (uriMatcher.match(uri) == i) {
				return VND_ANDROID_CURSOR_DIR + DBConstants.PROVIDER_NAME;
			} else if (uriMatcherIds.match(uri) == i) {
				return VND_ANDROID_CURSOR_ITEM + DBConstants.PROVIDER_NAME;
			}

		}
		throw new IllegalArgumentException("Unsupported URI: " + uri);
	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();

		boolean found = false;
		for (int i = 0; i < Tables.values().length; i++) {
			if (uriMatcher.match(uri) == i) {
				sqlBuilder.setTables(Tables.values()[i].name());
				found = true;
			} else if (uriMatcherIds.match(uri) == i) {
				sqlBuilder.setTables(Tables.values()[i].name());
				sqlBuilder.appendWhere(DBConstants._ID + EQUALS + uri.getPathSegments().get(1));
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
		for (int i = 0; i < Tables.values().length; i++) {
			if (uriMatcher.match(uri) == i) {
				count = appDataBase.update(Tables.values()[i].name(), values, selection, selectionArgs);
				found = true;
			} else if (uriMatcherIds.match(uri) == i) {
				count = appDataBase.update(Tables.values()[i].name(), values,
						DBConstants._ID + EQUALS + uri.getPathSegments().get(1) +
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
		for (int i = 0; i < Tables.values().length; i++) {
			if (uriMatcher.match(uri) == i || uriMatcherIds.match(uri) == i) {
				long rowID = appDataBase.insert(Tables.values()[i].name(), "", values);
				//---if added successfully---
				if (rowID > 0) {
					Uri _uri = ContentUris.withAppendedId(DBConstants.uriArray[i], rowID);
					getContext().getContentResolver().notifyChange(_uri, null);
					return _uri;
				}
			}
		}
//		throw new IllegalArgumentException("Unsupported URI: " + uri);

		throw new SQLException("Failed to insert row into " + uri); // TODO investigate better exception handling
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count = 0;
		boolean found = false;
		for (int i = 0; i < Tables.values().length; i++) {
			if (uriMatcher.match(uri) == i) {
				count = appDataBase.delete(Tables.values()[i].name(), selection, selectionArgs);
				found = true;
			} else if (uriMatcherIds.match(uri) == i) {
				String id = uri.getPathSegments().get(1);
				count = appDataBase.delete(
						Tables.values()[i].name(),
						DBConstants._ID + EQUALS + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""),
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
			// Init static tables first
			DBConstants dbConstants = new DBConstants();
			dbConstants.createMainTables();
			dbConstants.createUserStatsTables();
			dbConstants.createGameStatsTables();
			for (String createTableCall : dbConstants.createTablesArray) {
				db.execSQL(createTableCall);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			new AppData(context).clearPreferences(); // clear all values, to avoid class cast exceptions

			Log.w("Content provider database",
					"Upgrading database from version " +
							oldVersion + " to " + newVersion +
							", which will destroy all old data");
			// TODO handle backup data
			for (int i = 0; i < Tables.values().length; i++) {
				db.execSQL("DROP TABLE IF EXISTS " + Tables.values()[i]);
			}

			onCreate(db);
		}
	}
}
