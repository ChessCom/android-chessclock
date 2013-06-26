package com.chess.ui.fragments;

import android.app.Activity;
import com.chess.backend.LiveChessService;
import com.chess.lcc.android.DataNotValidException;
import com.chess.ui.activities.LiveBaseActivity;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.04.13
 * Time: 13:06
 */
public class LiveBaseFragment extends CommonLogicFragment {

	protected LiveBaseActivity liveBaseActivity;
	private LiveChessService liveService;
	protected boolean isLCSBound;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		liveBaseActivity = (LiveBaseActivity) activity;
		if (getAppData().isLiveChess()) {
			if (liveBaseActivity.isLCSBound()) {
				isLCSBound = true;
				liveService = liveBaseActivity.getLiveService();
			}
		}
	}

	protected LiveChessService getLiveService() throws DataNotValidException {
		LiveChessService service = liveBaseActivity.getLiveService();
		if (service == null || !service.isUserConnected()) {
			if (service == null) {
				throw new DataNotValidException(DataNotValidException.SERVICE_NULL);
			} else if (service.getLccHelper() == null) {
				throw new DataNotValidException(DataNotValidException.LCC_HELPER_NULL);
			} else if (service.getUser() == null) {
				throw new DataNotValidException(DataNotValidException.USER_NULL);
			} else /*if (!service.isConnected())*/ {
				throw new DataNotValidException(DataNotValidException.NOT_CONNECTED);
			}
		} else {
			return service;
		}
	}

	public void onLiveServiceConnected() {
	}

	public boolean isLCSBound() {
		return isLCSBound;
	}

	public void setLCSBound(boolean LCSBound) {
		isLCSBound = LCSBound;
		if (isLCSBound) {
			liveService = liveBaseActivity.getLiveService();
		}
	}

	protected void logoutFromLive() {
		if (isLCSBound) {
			liveService.logout();
			liveBaseActivity.unBindLiveService();
		}
	}

}
