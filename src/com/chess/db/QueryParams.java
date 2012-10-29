package com.chess.db;

import android.net.Uri;

/**
 * @author alien_roger
 * @created 27.10.12
 * @modified 27.10.12
 */
public class QueryParams {

	private boolean useRawQuery;

	private String fieldName;
	private String[] projection;
	private String order;
	private String[] arguments;
	private String selection;
	private String dbName;
	private Uri uri;
	private String commands;

	public QueryParams(Uri uri) {
		this.uri = uri;
	}

	public QueryParams() {

	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String[] getProjection() {
		return projection;
	}

	public void setProjection(String[] projection) {
		this.projection = projection;
	}

	public String getOrder() {
		return order;
	}

	public String[] getArguments() {
		return arguments;
	}

	public String getSelection() {
		return selection;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getCommands() {
		return commands;
	}

	public void setCommands(String commands) {
		this.commands = commands;
	}

	public boolean isUseRawQuery() {
		return useRawQuery;
	}

	public void setUseRawQuery(boolean useRawQuery) {
		this.useRawQuery = useRawQuery;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
}
