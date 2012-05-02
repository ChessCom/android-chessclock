package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;

import java.util.ArrayList;

public class OnlineGamesAdapter extends ArrayAdapter<GameListItem> {

	public ArrayList<GameListItem> items;
	private LayoutInflater inflater;
	private int textViewResId;
	private Context CTX;

	public OnlineGamesAdapter(Context context, int textViewResourceId, ArrayList<GameListItem> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.inflater = LayoutInflater.from(context);
		this.textViewResId = textViewResourceId;
		this.CTX = context;
	}

	public void setItemsList(ArrayList<GameListItem> list) {
		items = list;
		notifyDataSetChanged();
	}

	public boolean dropList(){
		items.clear();
		notifyDataSetChanged();
		return true;
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public GameListItem getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

//	@Override
//	public int getItemViewType(int position) {
//		return 0;
//	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(textViewResId, null);
		}
		final GameListItem gameListItem = items.get(position);
		if (gameListItem.type == GameListItem.LIST_TYPE_CHALLENGES) {
			if (gameListItem.isLiveChess) {
				try {	   // TODO stop eating exceptions
					if (gameListItem.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("1")
							&& gameListItem.values.get(GameListItem.IS_RELEASED_BY_ME).equals("0")) {
						convertView.findViewById(R.id.directChallenge).setVisibility(View.VISIBLE);
					} else {
						convertView.findViewById(R.id.directChallenge).setVisibility(View.GONE);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.d("OnlineGamesAdapter", AppConstants.SYMBOL_EMPTY + e);
				}
			}

			TextView info = (TextView) convertView.findViewById(R.id.info);
			TextView left = (TextView) convertView.findViewById(R.id.left);
			left.setText(AppConstants.SYMBOL_EMPTY);
			left.setVisibility(View.GONE);
			/*String color = CTX.getString(R.string.random);
					  if(el.values.get("playas_color").equals("1"))
					  {
						color = CTX.getString(R.string.white);
					  }
					  else if(el.values.get("playas_color").equals("2"))
					  {
						color = CTX.getString(R.string.black);
					  }*/
			final String time =/* gameListItem.isLiveChess ?
					gameListItem.values.get(GameListItem.IS_RATED) + ' ' + gameListItem.values.get(GameListItem.BASE_TIME) + gameListItem.values.get(GameListItem.TIME_INCREMENT)
					: */gameListItem.values.get(GameListItem.DAYS_PER_MOVE) + CTX.getString(R.string.days);
			String gametype = AppConstants.SYMBOL_EMPTY;
			if (gameListItem.values.get(GameListItem.GAME_TYPE) != null && gameListItem.values.get(GameListItem.GAME_TYPE).equals("2")) {
				gametype = " (960)";
			}

			/*System.out.println("!!!!!!!! isLiveChess = " + el.isLiveChess);
					  System.out.println("!!!!!!!! el = " + el);
					  System.out.println("!!!!!!!! el.values = " + el.values);
					  System.out.println("!!!!!!!! el.values.get(\"is_released_by_me\") = " + el.values.get("is_released_by_me"));
					  if (el.values.get("is_released_by_me") != null)
					  {
						System.out.println("!!!!!!!! el.values.get(\"is_released_by_me\").equals(\"1\") = " + el.values.get("is_released_by_me").equals("1"));
					  }
					  System.out.println("!!!!!!!! el.values.get(\"opponent_rating\") = " + el.values.get("opponent_rating"));*/

//				String opponentRating;   // TODO investigate why rated is null
//				if (el.isLiveChess && el.values.get("is_released_by_me").equals("1")) {
//					opponentRating = AppConstants.SYMBOL_EMPTY;
//				} else if (!el.isLiveChess && el.values.get("rated").equals("0")) {
//					opponentRating = "(" + el.values.get("opponent_rating") + ") Unrated";
//				} else {
//					opponentRating = "(" + el.values.get("opponent_rating") + ")";
//				}

			final String opponentRating =
					/*(gameListItem.isLiveChess && gameListItem.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1"))
							? AppConstants.SYMBOL_EMPTY : */"(" + gameListItem.values.get(GameListItem.OPPONENT_RATING) + ")";
			final String prefix =
					(gameListItem.isLiveChess && gameListItem.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("0")) && gameListItem.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1") ?
							"(open)" : gameListItem.values.get(GameListItem.OPPONENT_USERNAME);
			info.setText(prefix + ' ' + opponentRating + ' ' + gametype/* + ' ' + color*/ + "  " + time);
			//left.setText(time);

		} else if (gameListItem.type == GameListItem.LIST_TYPE_CURRENT) {

			TextView info = (TextView) convertView.findViewById(R.id.info);
			TextView left = (TextView) convertView.findViewById(R.id.left);
			String gametype = AppConstants.SYMBOL_EMPTY;
			if (gameListItem.values.get(GameListItem.GAME_TYPE) != null && gameListItem.values.get(GameListItem.GAME_TYPE).equals("2"))
				gametype = " (960)";
			String draw = AppConstants.SYMBOL_EMPTY;
			if (gameListItem.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
				draw = "\n" + CTX.getString(R.string.drawoffered);
			info.setText(gameListItem.values.get(GameListItem.OPPONENT_USERNAME) + gametype + draw);
			if (gameListItem.values.get(GameListItem.IS_MY_TURN).equals("1")) {
				left.setVisibility(View.VISIBLE);
				String amount = gameListItem.values.get(GameListItem.TIME_REMAINING_AMOUNT);
				if (gameListItem.values.get(GameListItem.TIME_REMAINING_AMOUNT).substring(0, 1).equals("0"))
					amount = amount.substring(1);
				if (gameListItem.values.get(GameListItem.TIME_REMAINING_UNITS).equals("h"))
					left.setText(amount + CTX.getString(R.string.hours));
				else
					left.setText(amount + CTX.getString(R.string.days));
			} else {
				left.setVisibility(View.GONE);
				left.setText(AppConstants.SYMBOL_EMPTY);
			}

		} else if (gameListItem.type == GameListItem.LIST_TYPE_FINISHED) {
			TextView info = (TextView) convertView.findViewById(R.id.info);
			TextView left = (TextView) convertView.findViewById(R.id.left);

			String gametype = AppConstants.SYMBOL_EMPTY;
			if (gameListItem.values.get(GameListItem.GAME_TYPE) != null
					&& gameListItem.values.get(GameListItem.GAME_TYPE).equals("2"))
				gametype = " (960)";


			String result = CTX.getString(R.string.lost);
			if (gameListItem.values.get(GameListItem.GAME_RESULT).equals("1")) {
				result = CTX.getString(R.string.won);
			} else if (gameListItem.values.get(GameListItem.GAME_RESULT).equals("2")) {
				result = CTX.getString(R.string.draw);
			}

			info.setText(gameListItem.values.get(GameListItem.OPPONENT_USERNAME) + gametype);
			left.setText(result);
		}

		return convertView;
	}
}
