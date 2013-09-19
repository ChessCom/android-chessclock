package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.ForumTopicItem;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.09.13
 * Time: 20:11
 */
public class ForumTopicsItemAdapter extends ItemsAdapter<ForumTopicItem.Topic> {

	public ForumTopicsItemAdapter(Context context, List<ForumTopicItem.Topic> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.newPostImg = (ImageView) view.findViewById(R.id.newPostImg);
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.lastCommentAgoTxt = (TextView) view.findViewById(R.id.lastCommentAgoTxt);
		holder.postsCountTxt = (TextView) view.findViewById(R.id.postsCountTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	protected void bindView(ForumTopicItem.Topic item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		long timestamp = item.getLastPostDate();
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.lastCommentAgoTxt.setText(lastCommentAgoStr + Symbol.BULLET);
		holder.titleTxt.setText(item.getSubject());

		int postCount = item.getPostCount();
		holder.postsCountTxt.setText(context.getString(R.string.posts_arg, postCount));

		if (haveNewPosts()) {
			holder.newPostImg.setImageResource(R.drawable.ic_new_post_t);
		} else {
			holder.newPostImg.setImageResource(R.drawable.ic_new_post_f);
		}
	}

	private boolean haveNewPosts() {
		return true;
	}

	protected class ViewHolder {
		public ImageView newPostImg;
		public TextView titleTxt;
		public TextView lastCommentAgoTxt;
		public TextView postsCountTxt;
	}
}
