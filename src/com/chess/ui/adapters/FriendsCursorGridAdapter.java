package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.01.13
 * Time: 17:28
 */
public class FriendsCursorGridAdapter extends ItemsCursorAdapter {

	private static final int IMG_SIZE = 50;

	public FriendsCursorGridAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.new_photo_thumb_view, parent, false);
		ViewHolder holder = new ViewHolder();
		ProgressImageView progressImageView = new ProgressImageView(context, IMG_SIZE);
		view.addView(progressImageView);
		holder.progressImage = progressImageView;

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
//		imageLoader.download(DBDataManager.getString(cursor, DBConstants.V_PHOTO_URL), holder.progressImage);
//		imageLoader.download("http://d1lalstwiwz2br.cloudfront.net/images_users/avatars/alien_roger.gif",
//		imageLoader.download("http://files.chesscomfiles.com/images_trophies/561-420.png",
		imageLoader.download("https://www.google.com/url?q=http://www.chess.com/forum/view/game-analysis/random-game-with-the-danish-gambit-refutation&sa=U&ei=GR0vUZfuDaOC4gSM9YGICQ&ved=0CAcQFjAAOB4&client=internal-uds-cse&usg=AFQjCNHvtGWT_A7LaNJtlWaFe2I5bf4sLA",
				holder.progressImage, IMG_SIZE);
	}

	private static class ViewHolder{
		public ProgressImageView progressImage;
	}
}
