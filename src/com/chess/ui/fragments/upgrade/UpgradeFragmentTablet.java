package com.chess.ui.fragments.upgrade;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboTextView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.11.13
 * Time: 13:28
 */
public class UpgradeFragmentTablet extends CommonLogicFragment {

	private static final String IMG_URL_1 = "http://images.chesscomfiles.com/images/icons/reviews/membership/peter.png";
	private static final String IMG_URL_2 = "http://images.chesscomfiles.com/images/icons/reviews/membership/lou.png";
	private LinearLayout featuresContainer1;
	private LinearLayout featuresContainer2;
	private LinearLayout featuresContainer3;
	private EnhancedImageDownloader imageDownloader;
	private int imageSize;
	private ProgressImageView quoteImg;
	private ProgressImageView quoteImg2;
	private LinearLayout diamondOptionsLay;
	private LinearLayout platinumOptionsLay;
	private LinearLayout goldOptionsLay;
	private String[] upgradeOptions;
	private LinearLayout.LayoutParams optionsParams;
	private LinearLayout.LayoutParams valuesParams;
	private int optionsTextSize;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.diamondFragmentContainer, UpgradeDetailsFragmentTablet.createInstance(UpgradeDetailsFragment.DIAMOND)
				, "Diamond");
		transaction.replace(R.id.platinumFragmentContainer, UpgradeDetailsFragmentTablet.createInstance(UpgradeDetailsFragment.PLATINUM)
				, "Platinum");
		transaction.replace(R.id.goldFragmentContainer, UpgradeDetailsFragmentTablet.createInstance(UpgradeDetailsFragment.GOLD)
				, "Gold");
		transaction.commitAllowingStateLoss();


		imageDownloader = new EnhancedImageDownloader(getActivity());
		imageSize = (int) (80 * density);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_upgrade_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.upgrade);

		widgetsInit(view);

	}

	@Override
	public void onResume() {
		super.onResume();

		imageDownloader.download(IMG_URL_1, quoteImg, imageSize);
		imageDownloader.download(IMG_URL_2, quoteImg2, imageSize);
	}

	private void widgetsInit(View view) {
		quoteImg = (ProgressImageView) view.findViewById(R.id.quoteImg);
		quoteImg2 = (ProgressImageView) view.findViewById(R.id.quoteImg2);

		Resources resources = getResources();
		{ // all features list
			featuresContainer1 = (LinearLayout) view.findViewById(R.id.featuresContainer1);
			featuresContainer2 = (LinearLayout) view.findViewById(R.id.featuresContainer2);
			featuresContainer3 = (LinearLayout) view.findViewById(R.id.featuresContainer3);

			String[] features1 = resources.getStringArray(R.array.upgrade_great_features_1);
			String[] features2 = resources.getStringArray(R.array.upgrade_great_features_2);
			String[] features3 = resources.getStringArray(R.array.upgrade_great_features_3);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			setFeatures(featuresContainer1, features1, params);
			setFeatures(featuresContainer2, features2, params);
			setFeatures(featuresContainer3, features3, params);

		}

		{// fill features comparison list
			diamondOptionsLay = (LinearLayout) view.findViewById(R.id.diamondOptionsLay);
			platinumOptionsLay = (LinearLayout) view.findViewById(R.id.platinumOptionsLay);
			goldOptionsLay = (LinearLayout) view.findViewById(R.id.goldOptionsLay);

			upgradeOptions = resources.getStringArray(R.array.upgrade_options);
			optionsParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsParams.weight = 1;

			valuesParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			optionsTextSize = (int) (resources.getDimensionPixelSize(R.dimen.upgrade_options_text_size) / density);

			String[] diamondValues = resources.getStringArray(R.array.upgrade_values_diamond);
			String[] platinumValues = resources.getStringArray(R.array.upgrade_values_platinum);
			String[] goldValues = resources.getStringArray(R.array.upgrade_values_gold);

			int diamondTextColor = resources.getColor(R.color.upgrade_diamond_sub_title);
			int platinumTextColor = resources.getColor(R.color.upgrade_platinum_sub_title);
			int goldTextColor = resources.getColor(R.color.upgrade_gold_sub_title);

//			int diamondBackColor =  resources.getColor(R.color.upgrade_diamond_button);
//			int platinumBackColor =  resources.getColor(R.color.upgrade_platinum_button);
//			int goldBackColor =  resources.getColor(R.color.upgrade_gold_button);

			int diamondBackResId = R.drawable.button_upgrade_diamond_flat;
			int platinumBackResId = R.drawable.button_upgrade_platinum_flat;
			int goldBackResId = R.drawable.button_upgrade_gold_flat;

			setOptionsValues(diamondOptionsLay, diamondValues, diamondBackResId, diamondTextColor);
			setOptionsValues(platinumOptionsLay, platinumValues, platinumBackResId, platinumTextColor);
			setOptionsValues(goldOptionsLay, goldValues, goldBackResId, goldTextColor);
		}
	}

	private void setOptionsValues(LinearLayout optionsLay, String[] values, int backResId, int color) {
		for (int i = 0; i < upgradeOptions.length; i++) {
			String option = upgradeOptions[i];
			String value = values[i];
			int padding = (int) (18 * density);
			LinearLayout linearLayout = new LinearLayout(getActivity());
			linearLayout.setOrientation(LinearLayout.HORIZONTAL);
			linearLayout.setMinimumHeight((int) (51 * density));
			linearLayout.setBackgroundResource(backResId);
			linearLayout.setPadding(padding, padding, padding, padding);
			linearLayout.setGravity(Gravity.CENTER_VERTICAL);

			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			int marginBetween = (int) (2 * density);
			layoutParams.setMargins(0, 0, 0, marginBetween);
			linearLayout.setLayoutParams(layoutParams);

			{// add option
				RoboTextView optionTxt = new RoboTextView(getActivity());
				optionTxt.setText(option);
				optionTxt.setTextColor(color);
				optionTxt.setFont(FontsHelper.BOLD_FONT);
				optionTxt.setTextSize(optionsTextSize);

				linearLayout.addView(optionTxt, optionsParams);
			}
			{// add value
				RoboTextView valueTxt = new RoboTextView(getActivity());
				valueTxt.setText(value);
				valueTxt.setTextColor(color);
				valueTxt.setFont(FontsHelper.BOLD_FONT);
				valueTxt.setTextSize(optionsTextSize);

				linearLayout.addView(valueTxt, valuesParams);

			}

			optionsLay.addView(linearLayout);
		}
	}

	private void setFeatures(LinearLayout featuresLinLay, String[] features1, LinearLayout.LayoutParams params) {
		for (String feature : features1) {
			RoboTextView featureTxt = new RoboTextView(getActivity());
			featureTxt.setText(feature);
			featureTxt.setTextColor(getResources().getColor(R.color.upgrade_diamond_sub_title));
			featureTxt.setFont(FontsHelper.BOLD_FONT);
			featureTxt.setTextSize(14);
			featureTxt.setPadding(0, 0, 0, (int) (6 * density));    // TODO remove hardcode

			featuresLinLay.addView(featureTxt, params);
		}
	}
}
