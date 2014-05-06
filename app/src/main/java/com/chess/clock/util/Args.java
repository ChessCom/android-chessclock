package com.chess.clock.util;

/**
 * <p>
 * This class has utility methods for common argument validations. Replaces <tt>if</tt> statements
 * at the forceStart of a method with more compact method calls.
 * <p/>
 * <p>
 * Example use case. Instead of:
 * <pre>
 *        public void doThis(String aText) {
 *            if (!Util.textHasContent(aText)) {
 *                throw new IllegalArgumentException();
 *            }
 *            //..main body elided
 *        }
 *        </pre>
 * </p>
 *
 * <p>
 * One may instead write:
 *
 * <pre>
 *        public void doThis(String aText) {
 *             Args.checkForContent(aText);
 *             //..main body elided
 *        }
 *        </pre>
 * </p>
 * </p>
 */
public final class Args {

	/**
	 * If <code>aText</code> does not satisfy {@link Args#textHasContent}, then throw an
	 * <code>IllegalArgumentException</code>. Most text used in an application is meaningful only
	 * if it has visible content.
	 */
	public static void checkForContent(String aText) {
		if (!textHasContent(aText)) {
			throw new IllegalArgumentException("Text has no visible content");
		}
	}

	/**
	 * If <tt>aNumber</tt> is less than <tt>1</tt>, then throw an <tt>IllegalArgumentException</tt>.
	 */
	public static void checkForPositive(long aNumber) {
		if (aNumber < 1) {
			throw new IllegalArgumentException(aNumber + " is less than 1");
		}
	}

	/**
	 * If <tt>aNumber</tt> is less than <tt>0</tt>, then throw an <tt>IllegalArgumentException</tt>.
	 */
	public static void checkForZeroOrNegative(long aNumber) {
		if (aNumber < 0) {
			throw new IllegalArgumentException(aNumber + " is less than 0");
		}
	}

	/**
	 * Returns true if aText is non-null and has visible content. This is a test which is often
	 * performed, and should probably be placed in a general utility class.
	 */
	public static boolean textHasContent(String aText) {
		String EMPTY_STRING = "";
		return (aText != null) && (!aText.trim().equals(EMPTY_STRING));
	}

	/**
	 * If <code>aObject</code> is null, then throw a <code>NullPointerException</code>.
	 */
	public static void checkForNull(Object aObject) {
		if (aObject == null) {
			throw new NullPointerException();
		}
	}
}
