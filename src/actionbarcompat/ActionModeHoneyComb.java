package actionbarcompat;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.05.13
 * Time: 7:02
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ActionModeHoneyComb extends ActionModeHelper implements ActionMode.Callback {

	public ActionModeHoneyComb(ActionBarActivity activity) {
		// TODO -> File | Settings | File Templates.

	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		return false;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {

	}
}
