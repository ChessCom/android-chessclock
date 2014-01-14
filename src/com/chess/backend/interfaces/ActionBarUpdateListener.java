package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import android.support.v4.app.Fragment;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.ui.activities.CoreActivityActionBar;

/**
 * ActionBarUpdateListener class
 *
 * @author alien_roger
 * @created at: 07.05.12 5:26
 */
public abstract class ActionBarUpdateListener<ItemType> extends AbstractUpdateListener<ItemType> {
	private ActionBarHelper actionBarHelper;
	private CoreActivityActionBar coreActivityActionBar;
	/**
	 * Use this constructor if you need it for fragment. It will handle getActivity() on updateData callback
	 * @param coreActivityActionBar
	 * @param clazz
	 * @param startedFragment
	 */
	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar, Fragment startedFragment, Class<ItemType> clazz) {
		super(coreActivityActionBar, startedFragment, clazz);
		init(coreActivityActionBar);
	}

	/**
	 * Use this constructor if you need it for fragment. It will handle getActivity() on updateData callback
	 * @param coreActivityActionBar
	 * @param startedFragment
	 */
	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar, Fragment startedFragment) {
		super(coreActivityActionBar, startedFragment);
		init(coreActivityActionBar);
	}

	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar, Class<ItemType> clazz) {
		super(coreActivityActionBar, clazz);
		init(coreActivityActionBar);
	}

	public ActionBarUpdateListener(CoreActivityActionBar coreActivityActionBar) {
		super(coreActivityActionBar);
		init(coreActivityActionBar);
	}

	private void init(CoreActivityActionBar coreActivityActionBar){
		this.coreActivityActionBar = coreActivityActionBar;
		actionBarHelper = coreActivityActionBar.provideActionBarHelper();

	}

	@Override
	public void showProgress(boolean show) {
		if(actionBarHelper != null) {
			actionBarHelper.setRefreshActionItemState(show);
		}
	}

	@Override
	public void errorHandle(Integer resultCode) {
		// show message only for re-login and app update
		if (RestHelper.containsServerCode(resultCode)) {
			int serverCode = RestHelper.decodeServerCode(resultCode);
			if (serverCode == ServerErrorCodes.ACCESS_DENIED_CODE) { // handled in CommonLogicFragment
				String message = coreActivityActionBar.getString(R.string.update_available_please_update);
				coreActivityActionBar.safeShowSinglePopupDialog(R.string.error, message);
				return;
			} else if (serverCode != ServerErrorCodes.INVALID_LOGIN_TOKEN_SUPPLIED) { // handled in CommonLogicFragment
				String serverMessage = ServerErrorCodes.getUserFriendlyMessage(coreActivityActionBar, serverCode); // TODO restore

				coreActivityActionBar.safeShowSinglePopupDialog(R.string.error, serverMessage);
				return;
			}
		}
		super.errorHandle(resultCode);
	}
}
