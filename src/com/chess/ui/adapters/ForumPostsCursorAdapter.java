package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;
import org.xml.sax.XMLReader;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 20:21
 */
public class ForumPostsCursorAdapter extends ItemsCursorAdapter {

	private final int imageSize;
	private final SparseArray<String> countryMap;
	private final SparseArray<Drawable> countryDrawables;
	private final ItemClickListenerFace clickFace;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public ForumPostsCursorAdapter(ItemClickListenerFace clickFace, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(clickFace.getMeContext(), cursor, imageFetcher);
		this.clickFace = clickFace;
		imageSize = resources.getDimensionPixelSize(R.dimen.chat_icon_size);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}
		countryDrawables = new SparseArray<Drawable>();

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_post_list_item, parent, false);
		ViewHolder holder = new ViewHolder();

		holder.photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		holder.quoteTxt = (TextView) view.findViewById(R.id.quoteTxt);
		holder.bodyTxt = (TextView) view.findViewById(R.id.bodyTxt);
		holder.commentNumberTxt = (TextView) view.findViewById(R.id.commentNumberTxt);

		view.setTag(holder);

		holder.quoteTxt.setOnClickListener(clickFace);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.quoteTxt.setTag(R.id.list_item_id, cursor.getPosition());

		holder.authorTxt.setText(getString(cursor, DbScheme.V_USERNAME));
		holder.commentNumberTxt.setText("# " + getInt(cursor, DbScheme.V_NUMBER));
		loadTextWithImage(holder.bodyTxt, getString(cursor, DbScheme.V_DESCRIPTION));

		long timestamp = getLong(cursor, DbScheme.V_CREATE_DATE);
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.dateTxt.setText(lastCommentAgoStr + Symbol.BULLET);

		// set premium icon
		int status = getInt(cursor, DbScheme.V_PREMIUM_STATUS);
		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(status));

		// set country flag
		int countryId = getInt(cursor, DbScheme.V_COUNTRY_ID);
		Drawable drawable;
		if (countryDrawables.get(countryId) == null) {
			drawable = AppUtils.getCountryFlagScaled(context, countryMap.get(countryId));
			countryDrawables.put(countryId, drawable);
		} else {
			drawable = countryDrawables.get(countryId);
		}

		holder.countryImg.setImageDrawable(drawable);

		String avatarUrl = getString(cursor, DbScheme.V_PHOTO_URL);
		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.photoImg.getImageView());
	}

	protected class ViewHolder {
		public ProgressImageView photoImg;
		public TextView authorTxt;
		public ImageView countryImg;
		public ImageView premiumImg;
		public TextView dateTxt;
		public TextView quoteTxt;
		public TextView bodyTxt;
		public TextView commentNumberTxt;
	}

	public class MyHtmlTagHandler implements Html.TagHandler {

		@Override
		public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
			if (tag.equalsIgnoreCase("strike") || tag.equals("s")) {
				processStrike(opening, output);
			}
		}

		private void processStrike(boolean opening, Editable output) {
			int len = output.length();
			if (opening) {
				output.setSpan(new StrikethroughSpan(), len, len, Spannable.SPAN_MARK_MARK);
			} else {
				Object obj = getLast(output, StrikethroughSpan.class);
				int where = output.getSpanStart(obj);

				output.removeSpan(obj);

				if (where != len) {
					output.setSpan(new StrikethroughSpan(), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}

		private Object getLast(Editable text, Class kind) {
			Object[] spans = text.getSpans(0, text.length(), kind);

			if (spans.length == 0) {
				return null;
			} else {
				for (int i = spans.length; i > 0; i--) {
					if (text.getSpanFlags(spans[i - 1]) == Spannable.SPAN_MARK_MARK) {
						return spans[i - 1];
					}
				}
				return null;
			}
		}
	}
}
