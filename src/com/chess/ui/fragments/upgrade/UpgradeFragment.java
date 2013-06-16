package com.chess.ui.fragments.upgrade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.02.13
 * Time: 21:23
 */
public class UpgradeFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_upgrade_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.upgrade);

		view.findViewById(R.id.diamondBtnLay).setOnClickListener(this);
		view.findViewById(R.id.platinumBtnLay).setOnClickListener(this);
		view.findViewById(R.id.goldBtnLay).setOnClickListener(this);
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
}
