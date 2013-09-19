package com.chess.statics;

/**
 * Symbol constants
 */
public class Symbol {
	public static final String SPACE = " ";
	public static final String NEW_STR = "\n";
	public static final String EMPTY = "";
	public static final String COMMA = ",";
	public static final String LEFT_PAR = "(";
	public static final String RIGHT_PAR = ")";
	public static final String PLUS = "+";
	public static final String MINUS = "-";
	public static final String TAG = "<";
	public static final String COLON = ":";
	public static final String SLASH = "|";
	public static final String EX = "!";
	public static final String DOT = ". ";
	public static final String BULLET = SPACE + "\u2022";
	public static final String AMP_CODE = "&amp;";
	public static final String AMP = "&";
	public static final String PERCENT = "%";
	public static final String QUESTION = "?";

	public static String wrapInPars(String string) {return LEFT_PAR + string + RIGHT_PAR;}
	public static String wrapInPars(int value) {return LEFT_PAR + String.valueOf(value) + RIGHT_PAR;}
}