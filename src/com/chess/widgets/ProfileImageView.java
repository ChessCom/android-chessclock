package com.chess.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.14
 * Time: 10:51
 */
public class ProfileImageView extends ImageView implements View.OnClickListener {

	private String username;
	private ProfileOpenFace profileOpenFace;

	public interface ProfileOpenFace{

		void openProfile(String username);
	}

	public ProfileImageView(Context context) {
		super(context);
		onCreate(context);
	}

	public ProfileImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(context);
	}

	private void onCreate(Context context) {
	}

	public void setUsername(String username, ProfileOpenFace profileOpenFace) {
		this.username = username;
		this.profileOpenFace = profileOpenFace;
		setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if (profileOpenFace != null) {
			profileOpenFace.openProfile(username);
		}
	}
}
