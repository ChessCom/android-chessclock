package com.chess.ui.fragments.upgrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.statics.StaticData;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.02.13
 * Time: 21:23
 */
public class UpgradeFragment extends CommonLogicFragment {

	private static final String IMG_URL = "http://images.chesscomfiles.com/images/icons/reviews/membership/peter.png";
	private ProgressImageView quoteImg;
	private EnhancedImageDownloader imageDownloader;
	private int imageSize;
	private View goldBtnLay;
	private View platinumBtnLay;
	private View disabledGoldView;
	private View disabledPlatinumView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageDownloader = new EnhancedImageDownloader(getActivity());
		imageSize = (int) (getResources().getDimension(R.dimen.daily_list_item_image_size) / density);


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_upgrade_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.upgrade);

		quoteImg = (ProgressImageView) view.findViewById(R.id.quoteImg);

		view.findViewById(R.id.diamondBtnLay).setOnClickListener(this);
		platinumBtnLay = view.findViewById(R.id.platinumBtnLay);
		platinumBtnLay.setOnClickListener(this);
		goldBtnLay = view.findViewById(R.id.goldBtnLay);
		goldBtnLay.setOnClickListener(this);

		disabledGoldView = view.findViewById(R.id.disabledGoldView);
		disabledPlatinumView = view.findViewById(R.id.disabledPlatinumView);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.diamondBtnLay) {
			getActivityFace().openFragment(UpgradeDetailsFragment.createInstance(UpgradeDetailsFragment.DIAMOND));
		} else if (v.getId() == R.id.platinumBtnLay) {
			getActivityFace().openFragment(UpgradeDetailsFragment.createInstance(UpgradeDetailsFragment.PLATINUM));
		} else if (v.getId() == R.id.goldBtnLay) {
			getActivityFace().openFragment(UpgradeDetailsFragment.createInstance(UpgradeDetailsFragment.GOLD));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// get user membership level
		int premiumStatus = getAppData().getUserPremiumStatus();

		if (premiumStatus == StaticData.PLATINUM_USER) { // disable gold
			goldBtnLay.setEnabled(false);
			disabledGoldView.setVisibility(View.VISIBLE);
		} else if (premiumStatus == StaticData.DIAMOND_USER) { // disable platinum & gold
			platinumBtnLay.setEnabled(false);
			goldBtnLay.setEnabled(false);
			disabledPlatinumView.setVisibility(View.VISIBLE);
			disabledGoldView.setVisibility(View.VISIBLE);
		} else {
			platinumBtnLay.setEnabled(true);
			goldBtnLay.setEnabled(true);
			disabledPlatinumView.setVisibility(View.GONE);
			disabledGoldView.setVisibility(View.GONE);
		}

		imageDownloader.download(IMG_URL, quoteImg, imageSize);
	}
}
