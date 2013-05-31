package actionbarcompat;

import android.os.Build;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.05.13
 * Time: 6:59
 */
public class ActionModeHelper  {

	public static ActionModeHelper createInstance(ActionBarActivity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new ActionModeHoneyComb(activity);
		} else {
			return new ActionModeBase(activity);
		}
	}
}
