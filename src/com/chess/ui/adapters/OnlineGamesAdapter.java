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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(textViewResId, null);
		}
		final GameListItem el = items.get(position);
		if (el != null) {
			if (el.type == GameListItem.LIST_TYPE_CHALLENGES) {
//			if (el.type ==  GameListItem.LIST_TYPE_CURRENT) {
				if (el.isLiveChess) {
					try {	   // TODO stop eating exceptions

						/*System.out.println("!!!!!!!! items = " + items);
									  System.out.println("!!!!!!!! el.values = " + el.values);
									  System.out.println("!!!!!!!! el.values.get(is_direct_challenge) = " + el.values.get("is_direct_challenge"));
									  System.out.println("!!!!!!!! el.values.get(is_released_by_me) = " + el.values.get("is_released_by_me"));*/
						if (el.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("1")
								&& el.values.get(GameListItem.IS_RELEASED_BY_ME).equals("0")) {
							convertView.findViewById(R.id.directChallenge).setVisibility(View.VISIBLE);
						} else {
							convertView.findViewById(R.id.directChallenge).setVisibility(View.GONE);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.d("OnlineGamesAdapter", "" + e);
					}
				}

				TextView info = (TextView) convertView.findViewById(R.id.info);
				TextView left = (TextView) convertView.findViewById(R.id.left);
				/*String color = CTX.getString(R.string.random);
						  if(el.values.get("playas_color").equals("1"))
						  {
							color = CTX.getString(R.string.white);
						  }
						  else if(el.values.get("playas_color").equals("2"))
						  {
							color = CTX.getString(R.string.black);
						  }*/
				final String time = el.isLiveChess ?
						el.values.get(GameListItem.IS_RATED) + ' ' + el.values.get(GameListItem.BASE_TIME) + el.values.get(GameListItem.TIME_INCREMENT)
						: el.values.get(GameListItem.DAYS_PER_MOVE) + CTX.getString(R.string.days);
				String gametype = "";
				if (el.values.get(GameListItem.GAME_TYPE) != null && el.values.get(GameListItem.GAME_TYPE).equals("2")) {
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
//					opponentRating = "";
//				} else if (!el.isLiveChess && el.values.get("rated").equals("0")) {
//					opponentRating = "(" + el.values.get("opponent_rating") + ") Unrated";
//				} else {
//					opponentRating = "(" + el.values.get("opponent_rating") + ")";
//				}

				final String opponentRating =
						(el.isLiveChess && el.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1"))
								? "" : "(" + el.values.get(GameListItem.OPPONENT_RATING) + ")";
				final String prefix =
						(el.isLiveChess && el.values.get(GameListItem.IS_DIRECT_CHALLENGE).equals("0")) && el.values.get(GameListItem.IS_RELEASED_BY_ME).equals("1") ?
								"(open)" : el.values.get(GameListItem.OPPONENT_USERNAME);
				info.setText(prefix + ' ' + opponentRating + ' ' + gametype/* + ' ' + color*/ + "  " + time);
				//left.setText(time);

//			} else if (el.type == GameListItem.LIST_TYPE_CHALLENGES) {
			} else if (el.type == GameListItem.LIST_TYPE_CURRENT) {

				TextView info = (TextView) convertView.findViewById(R.id.info);
				TextView left = (TextView) convertView.findViewById(R.id.left);
				String gametype = "";
				if (el.values.get(GameListItem.GAME_TYPE) != null && el.values.get(GameListItem.GAME_TYPE).equals("2"))
					gametype = " (960)";
				String draw = "";
				if (el.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
					draw = "\n" + CTX.getString(R.string.drawoffered);
				info.setText(el.values.get(GameListItem.OPPONENT_USERNAME) + gametype + draw);
				if (el.values.get(GameListItem.IS_MY_TURN).equals("1")) {
					left.setVisibility(View.VISIBLE);
					String amount = el.values.get(GameListItem.TIME_REMAINING_AMOUNT);
					if (el.values.get(GameListItem.TIME_REMAINING_AMOUNT).substring(0, 1).equals("0"))
						amount = amount.substring(1);
					if (el.values.get(GameListItem.TIME_REMAINING_UNITS).equals("h"))
						left.setText(amount + CTX.getString(R.string.hours));
					else
						left.setText(amount + CTX.getString(R.string.days));
				} else {
					left.setVisibility(View.GONE);
					left.setText("");
				}

			} else if (el.type == GameListItem.LIST_TYPE_FINISHED) {
				TextView info = (TextView) convertView.findViewById(R.id.info);
				TextView left = (TextView) convertView.findViewById(R.id.left);

				String gametype = "";
				if (el.values.get(GameListItem.GAME_TYPE) != null && el.values.get(GameListItem.GAME_TYPE).equals("2"))
					gametype = " (960)";


				String result = CTX.getString(R.string.lost);
				if (el.values.get(GameListItem.GAME_RESULT).equals("1")) {
					result = CTX.getString(R.string.won);
				} else if (el.values.get(GameListItem.GAME_RESULT).equals("2")) {
					result = CTX.getString(R.string.draw);
				}

				info.setText(el.values.get(GameListItem.OPPONENT_USERNAME) + gametype);
				left.setText(result);
			}
		}

		return convertView;
	}
}
