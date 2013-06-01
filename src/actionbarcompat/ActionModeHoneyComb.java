package actionbarcompat;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.05.13
 * Time: 7:02
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ActionModeHoneyComb extends ActionModeHelper implements ActionMode.Callback {

	private ActionBarActivity activity;

	public ActionModeHoneyComb(ActionBarActivity activity) {
		this.activity = activity;
	}

	@Override
	public void startActionMode() {
		 activity.startActionMode(this);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		if (contextMenuId == 0) {
			return true;
		}
		// Inflate a menu resource providing context menu items
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(contextMenuId, menu);
		return true;
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
		editFace.onDoneClicked();
	}
}
