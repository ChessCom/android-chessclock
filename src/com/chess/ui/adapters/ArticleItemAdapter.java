package com.chess.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.ArticleItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.09.13
 * Time: 16:34
 */
public class ArticleItemAdapter extends ItemsAdapter<ArticleItem.Data> {

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private int PHOTO_SIZE;
	private CharacterStyle foregroundSpan;
	private Date date;
	private SparseBooleanArray viewedMap;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;


	public ArticleItemAdapter(Context context, List<ArticleItem.Data> itemList, SmartImageFetcher imageFetcher) {
		super(context, itemList, imageFetcher);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();

		date = new Date();

		PHOTO_SIZE = (int) context.getResources().getDimension(R.dimen.article_thumb_width);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_article_thumb_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.thumbnailImg = (ProgressImageView) view.findViewById(R.id.thumbnailImg);
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	protected void bindView(ArticleItem.Data item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String firstName = item.getFirstName();
		String chessTitle = item.getChessTitle();
		String lastName = item.getLastName();
		CharSequence authorStr;
		if (TextUtils.isEmpty(chessTitle)) {
			authorStr = firstName + Symbol.SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(item.getTitle());
		date.setTime(item.getCreateDate() * 1000L);
		holder.dateTxt.setText(dateFormatter.format(date));

		String imageUrl = item.getImageUrl();
		if (!imageDataMap.containsKey(imageUrl)) {
			imageDataMap.put(imageUrl, new SmartImageFetcher.Data(imageUrl, PHOTO_SIZE));
		}

		imageFetcher.loadImage(imageDataMap.get(imageUrl), holder.thumbnailImg.getImageView());

		if (viewedMap.get((int) item.getId(), false)) {
			holder.titleTxt.setTextColor(watchedTextColor);
		} else {
			holder.titleTxt.setTextColor(unWatchedTextColor);
		}
	}

	public void addViewedMap(SparseBooleanArray viewedArticlesMap) {
		this.viewedMap = viewedArticlesMap;
	}

	protected class ViewHolder {
		public ProgressImageView thumbnailImg;
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}
}
