package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.08.13
 * Time: 19:38
 */
public class AvatarView extends ProgressImageView {


	private static final float BIG_SIZE = 15;
	private static final float SMALL_SIZE = 7.5f;

	private ImageView onlineBadge;

	public AvatarView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AvatarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public AvatarView(Context context, int size) {
		super(context, size);
	}

	@Override
	protected void onCreate(AttributeSet attrs) {
		super.onCreate(attrs);

		int indicatorSize;
		if (size > DEFAULT_IMG_SIZE) {
			indicatorSize = (int) (BIG_SIZE * density);
		} else {
			indicatorSize = (int) (SMALL_SIZE * density);
		}

		// add isOnline indication
		{// image  // TODO add white border for more visibility
			onlineBadge = new ImageView(getContext());
			LayoutParams indicatorParams = new LayoutParams(indicatorSize, indicatorSize);
			indicatorParams.addRule(ALIGN_PARENT_RIGHT);
			indicatorParams.addRule(ALIGN_PARENT_BOTTOM);

			onlineBadge.setVisibility(GONE);
			onlineBadge.setAdjustViewBounds(true);
			onlineBadge.setScaleType(ImageView.ScaleType.FIT_XY);
			int onlineColor = getResources().getColor(R.color.is_online_color);
			ColorDrawable drawable = new ColorDrawable(onlineColor);
			onlineBadge.setImageDrawable(drawable);

			addView(onlineBadge, indicatorParams);
		}
	}

	public void setOnline(boolean online) {
		onlineBadge.setVisibility(online ? VISIBLE : GONE);
	}
}
