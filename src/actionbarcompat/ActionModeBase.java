package actionbarcompat;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.05.13
 * Time: 7:02
 */

public class ActionModeBase extends ActionModeHelper{

	private ActionBarActivity activity;

	public ActionModeBase(ActionBarActivity activity) {
		this.activity = activity;
	}

	@Override
	public void startActionMode() {
		activity.getActionBarHelper().showActionMode(true);
		activity.getActionBarHelper().setDoneClickListener(editFace);
	}


}
