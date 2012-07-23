package com.chess.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.chess.R;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.model.GameListCurrentItem;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;

import java.util.List;


public class AlarmReceiverNew extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// http://www.chess.com/api/v2/get_echess_current_games

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(context));
		loadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ONLY_USER_TURN);
		new GetStringObjTask(new UpdateListener(context)).executeTask(loadItem);

	}

	private class UpdateListener extends AbstractUpdateListener<String> {
		public UpdateListener(Context context) {
			super(context);
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				List<GameListCurrentItem> itemList = ChessComApiParser.getCurrentOnlineGames(returnedObj);
				long gameId  = 0;
				for (GameListCurrentItem gameListItem : itemList) {
					gameId = gameListItem.getGameId();
				}

/*
		Success+<total_games_returned>:
		(<game_id>:
		<opponent_username>:
		<time_remaining_amount>:
		<last_move_from_square>:
		<last_move_to_square>:
		<is_opponent_online>)0-n
*/

				AppUtils.showNewMoveStatusNotification(getMeContext(),
						getMeContext().getString(R.string.your_move),
						getMeContext().getString(R.string.online_challenge_wait),
						StaticData.MOVE_REQUEST_CODE,
						gameId);

				getMeContext().sendBroadcast(new Intent(IntentConstants.CHALLENGES_LIST_UPDATE));
			}
		}
	}

}
