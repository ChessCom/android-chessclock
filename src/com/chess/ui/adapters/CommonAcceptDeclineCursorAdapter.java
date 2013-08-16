package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.08.13
 * Time: 20:45
 */
public class CommonAcceptDeclineCursorAdapter extends ItemsCursorAdapter {

	private final ItemClickListenerFace clickListenerFace;
	private final int imageSize;

	public CommonAcceptDeclineCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor) {
		super(clickListenerFace.getMeContext(), cursor);
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_challenge_game_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.acceptBtn = (TextView) view.findViewById(R.id.acceptBtn);
		holder.cancelBtn = (TextView) view.findViewById(R.id.cancelBtn);

		holder.acceptBtn.setOnClickListener(clickListenerFace);
		holder.cancelBtn.setOnClickListener(clickListenerFace);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.cancelBtn.setTag(itemListId, cursor.getPosition());
		holder.acceptBtn.setTag(itemListId, cursor.getPosition());

		holder.playerTxt.setText(getString(cursor, DbScheme.V_USERNAME));
		String avatarUrl = getString(cursor, DbScheme.V_USER_AVATAR);
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView cancelBtn;
		public TextView acceptBtn;
	}

}
