package com.chess.backend.image_load;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.08.13
 * Time: 19:38
 */
public class AvatarView extends ProgressImageView {

	public static final float INDICATOR_SCALE_FACTOR = 0.2542f;
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

		int indicatorSize = (int) (size * INDICATOR_SCALE_FACTOR);

		// add isOnline indication
		{// image
			onlineBadge = new ImageView(getContext());
			LayoutParams indicatorParams = new LayoutParams(indicatorSize, indicatorSize);
			indicatorParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;

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
