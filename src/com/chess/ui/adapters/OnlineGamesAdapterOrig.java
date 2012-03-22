package com.chess.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.GameListElement;
import com.chess.model.GameListElementOrig;

import java.util.ArrayList;

public class OnlineGamesAdapterOrig extends ArrayAdapter<GameListElement> {

	public ArrayList<GameListElement> items;
	private LayoutInflater vi;
	private int res;
	private Context CTX;

	public OnlineGamesAdapterOrig(Context context, int textViewResourceId, ArrayList<GameListElement> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.vi = LayoutInflater.from(context);
		this.res = textViewResourceId;
		this.CTX = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = vi.inflate(res, null);
		}
		final GameListElement el = items.get(position);
		if (el != null) {
			if (el.type == GameListElement.LIST_TYPE_CHALLENGES) {
				if (el.isLiveChess) {
					try {

						/*System.out.println("!!!!!!!! items = " + items);
									  System.out.println("!!!!!!!! el.values = " + el.values);
									  System.out.println("!!!!!!!! el.values.get(is_direct_challenge) = " + el.values.get("is_direct_challenge"));
									  System.out.println("!!!!!!!! el.values.get(is_released_by_me) = " + el.values.get("is_released_by_me"));*/
						if (el.values.get("is_direct_challenge").equals("1") && el.values.get("is_released_by_me").equals("0")) {
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
						el.values.get("is_rated") + ' ' + el.values.get("base_time") + el.values.get("time_increment")
						: el.values.get("days_per_move") + CTX.getString(R.string.days);
				String gametype = "";
				if (el.values.get("game_type") != null && el.values.get("game_type").equals("2")) {
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

				String opponentRating;
				if (el.isLiveChess && el.values.get("is_released_by_me").equals("1")) {
					opponentRating = "";
				} else if (!el.isLiveChess && el.values.get("rated").equals("0")) {
					opponentRating = "(" + el.values.get("opponent_rating") + ") Unrated";
				} else {
					opponentRating = "(" + el.values.get("opponent_rating") + ")";
				}

				/*final String opponentRating =
							(el.isLiveChess && el.values.get("is_released_by_me").equals("1")) ? "" : "(" + el.values.get("opponent_rating") + ")";*/
				final String prefix =
						(el.isLiveChess && el.values.get("is_direct_challenge").equals("0")) && el.values.get("is_released_by_me").equals("1") ?
								"(open)" : el.values.get("opponent_username");
				info.setText(prefix + ' ' + opponentRating + ' ' + gametype/* + ' ' + color*/ + "  " + time);
				//left.setText(time);
			} else if (el.type == GameListElementOrig.LIST_TYPE_GAMES) {
				TextView info = (TextView) convertView.findViewById(R.id.info);
				TextView left = (TextView) convertView.findViewById(R.id.left);
				String gametype = "";
				if (el.values.get("game_type") != null && el.values.get("game_type").equals("2"))
					gametype = " (960)";
				String draw = "";
				if (el.values.get("is_draw_offer_pending").equals("p"))
					draw = "\n" + CTX.getString(R.string.drawoffered);
				info.setText(el.values.get("opponent_username") + gametype + draw);
				if (el.values.get("is_my_turn").equals("1")) {
					left.setVisibility(View.VISIBLE);
					String amount = el.values.get("time_remaining_amount");
					if (el.values.get("time_remaining_amount").substring(0, 1).equals("0"))
						amount = amount.substring(1);
					if (el.values.get("time_remaining_units").equals("h"))
						left.setText(amount + CTX.getString(R.string.hours));
					else
						left.setText(amount + CTX.getString(R.string.days));
				} else {
					left.setVisibility(View.GONE);
					left.setText("");
				}

			} else if (el.type == GameListElement.LIST_TYPE_FINISHED) {
				TextView info = (TextView) convertView.findViewById(R.id.info);
				TextView left = (TextView) convertView.findViewById(R.id.left);

				String gametype = "";
				if (el.values.get("game_type") != null && el.values.get("game_type").equals("2"))
					gametype = " (960)";


				String result = CTX.getString(R.string.lost);
				if (el.values.get("game_result").equals("1")) {
					result = CTX.getString(R.string.won);
				} else if (el.values.get("game_result").equals("2")) {
					result = CTX.getString(R.string.draw);
				}

				info.setText(el.values.get("opponent_username") + gametype);
				left.setText(result);
			}
		}

		return convertView;
	}
}