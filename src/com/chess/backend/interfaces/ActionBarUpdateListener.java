package com.chess.backend.interfaces;

import actionbarcompat.ActionBarHelper;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCode;
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
	public void errorHandle(String resultMessage) {
		coreActivityActionBar.safeShowSinglePopupDialog(R.string.error, resultMessage);
	}

	@Override
	public void errorHandle(Integer resultCode) {
		super.errorHandle(resultCode);

		// show message only for re-login
		if (RestHelper.containsServerCode(resultCode)) {
			int serverCode = RestHelper.decodeServerCode(resultCode);
			if (serverCode == ServerErrorCode.INVALID_LOGIN_TOKEN_SUPPLIED) {
				String serverMessage = ServerErrorCode.getUserFriendlyMessage(coreActivityActionBar, serverCode); // TODO restore

				coreActivityActionBar.safeShowSinglePopupDialog(R.string.error, serverMessage);
			}
		}
	}
}
