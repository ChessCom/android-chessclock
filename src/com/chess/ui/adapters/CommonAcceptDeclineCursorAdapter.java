package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.08.13
 * Time: 20:45
 */
public class CommonAcceptDeclineCursorAdapter extends ItemsCursorAdapter {

	private final ItemClickListenerFace clickListenerFace;
	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public CommonAcceptDeclineCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(clickListenerFace.getMeContext(), cursor, imageFetcher);
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		this.clickListenerFace = clickListenerFace;
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.daily_challenge_game_item, parent, false);
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

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView cancelBtn;
		public TextView acceptBtn;
	}

}
