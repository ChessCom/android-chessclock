package actionbarcompat;

import android.os.Build;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.05.13
 * Time: 6:59
 */
public abstract class ActionModeHelper {

	protected EditFace editFace;
	protected int contextMenuId;

	public static ActionModeHelper createInstance(ActionBarActivity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			return new ActionModeHoneyComb(activity);
		} else {
			return new ActionModeBase(activity);
		}
	}

	public void setEditFace(EditFace editFace) {
		this.editFace = editFace;
	}

	public void setContextMenuId(int contextMenuId) {
		this.contextMenuId = contextMenuId;
	}

	public abstract void startActionMode();

	public interface EditFace {
		void onDoneClicked();
	}

}
