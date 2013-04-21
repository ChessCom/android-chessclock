package com.chess.ui.fragments.upgrade;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.StaticData;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.02.13
 * Time: 18:45
 */
public class UpgradeDetailsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

	public static final int DIAMOND = 0;
	public static final int PLATINUM = 1;
	public static final int GOLD = 2;

	private static final String PLAN = "plan";

	private static final String DIAMOND_MONTH_PAY = "$14";
	private static final String DIAMOND_YEAR_PAY = "$99";
	private static final String PLATINUM_MONTH_PAY = "$7";
	private static final String PLATINUM_YEAR_PAY = "$49";
	private static final String GOLD_MONTH_PAY = "$5";
	private static final String GOLD_YEAR_PAY = "$29";
	private static final String YEAR_DISCOUNT = "40%";


	private RadioGroup radioGroup;
	private View planDetailsView;
	private View topView;
	private ImageView planImg;
	private TextView planTitleTxt;
	private TextView planSubTitleTxt;
	private View periodView;
	private View monthView;
	private TextView monthValueTxt;
	private TextView monthLabelTxt;
	private CheckBox monthCheckBox;
	private View yearView;
	private TextView yearValueTxt;
	private TextView yearLabelTxt;
	private CheckBox yearCheckBox;
	private Button setPlanBtn;
	private LinearLayout descriptionView;

	private float density;
	private PlanConfig[] configs;

	public static UpgradeDetailsFragment newInstance(int code){
		UpgradeDetailsFragment frag = new UpgradeDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(PLAN, code);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_upgrade_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.upgrade);

		density = getResources().getDisplayMetrics().density;

		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(this);


		planDetailsView = view.findViewById(R.id.planDetailsView);
		topView = view.findViewById(R.id.topView);
		planImg = (ImageView) view.findViewById(R.id.planImg);
		planTitleTxt = (TextView) view.findViewById(R.id.planTitleTxt);
		planSubTitleTxt = (TextView) view.findViewById(R.id.planSubTitleTxt);
		periodView = view.findViewById(R.id.periodView);
		monthView = view.findViewById(R.id.monthView);
		monthValueTxt = (TextView) view.findViewById(R.id.monthValueTxt);
		monthLabelTxt = (TextView) view.findViewById(R.id.monthLabelTxt);
		monthCheckBox = (CheckBox) view.findViewById(R.id.monthCheckBox);
		monthCheckBox.setOnCheckedChangeListener(this);
		yearView = view.findViewById(R.id.yearView);
		yearValueTxt = (TextView) view.findViewById(R.id.yearValueTxt);
		yearLabelTxt = (TextView) view.findViewById(R.id.yearLabelTxt);
		yearCheckBox = (CheckBox) view.findViewById(R.id.yearCheckBox);
		yearCheckBox.setOnCheckedChangeListener(this);
		TextView yearDiscountTxt = (TextView) view.findViewById(R.id.yearDiscountTxt);
		setPlanBtn = (Button) view.findViewById(R.id.setPlanBtn);
		descriptionView = (LinearLayout) view.findViewById(R.id.descriptionView);

		yearDiscountTxt.setText(getString(R.string.save) + StaticData.SYMBOL_NEW_STR + YEAR_DISCOUNT);

		configs = new PlanConfig[3];

		configs[0] = getDiamondConfig();
		configs[1] = getPlatinumConfig();
		configs[2] = getGoldConfig();
	}

	@Override
	public void onResume() {
		super.onResume();
		radioGroup.check(getIdForPlan());
	}



	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateData();
	}

	private void updateData() {
		switch (radioGroup.getCheckedRadioButtonId()) {
			case R.id.diamondBtn:
				showPaymentPlan(configs[DIAMOND]);
				break;
			case R.id.platinumBtn:
				showPaymentPlan(configs[PLATINUM]);
				break;
			case R.id.goldBtn:
				showPaymentPlan(configs[GOLD]);
				break;
		}
	}

	private void showPaymentPlan(PlanConfig planConfig) {
		planDetailsView.setBackgroundResource(planConfig.planDetailsBack);
		planDetailsView.setPadding(0, 0, 0, (int) (12 * density));
		planImg.setImageResource(planConfig.planIconId);

		planTitleTxt.setTextColor(planConfig.titleColor);
		planTitleTxt.setText(planConfig.titleId);

		planSubTitleTxt.setTextColor(planConfig.subTitleColor);
		planSubTitleTxt.setText(planConfig.subTitleId);

		// per month
		monthView.setBackgroundResource(planConfig.payViewColorId);
		monthView.setPadding(0, (int) (12 * density), 0, (int) (16 * density));

		monthValueTxt.setText(planConfig.monthValue);
		monthValueTxt.setTextColor(planConfig.titleColor);

		monthLabelTxt.setTextColor(planConfig.subTitleColor);
		monthCheckBox.setButtonDrawable(planConfig.checkBoxDrawableId);

		// per year
		yearView.setBackgroundResource(planConfig.payViewColorId);
		yearView.setPadding(0, (int) (12 * density), 0, (int) (16 * density));

		yearValueTxt.setText(planConfig.yearValue);
		yearValueTxt.setTextColor(planConfig.titleColor);

		yearLabelTxt.setTextColor(planConfig.subTitleColor);
		yearCheckBox.setButtonDrawable(planConfig.checkBoxDrawableId);
		setPlanBtn.setBackgroundResource(planConfig.buttonBackId);
		descriptionView.setBackgroundResource(planConfig.descriptionBackId);
		int padding = (int) (15 * density);
		descriptionView.setPadding(padding, padding, padding, padding);

		descriptionView.removeAllViews();

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		RoboTextView unlockTitleTxt = new RoboTextView(getActivity());

		unlockTitleTxt.setTextColor(planConfig.subTitleColor);
		unlockTitleTxt.setText(planConfig.unlockFeaturesTitleId);
		unlockTitleTxt.setFont(RoboTextView.ITALIC_FONT);
		unlockTitleTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		unlockTitleTxt.setPadding(0,0,0, (int) (7 * density));
		descriptionView.addView(unlockTitleTxt, params);

		for (String feature : planConfig.features) {
			RoboTextView featureTxt = new RoboTextView(getActivity());
			featureTxt.setText(feature);
			featureTxt.setTextColor(planConfig.subTitleColor);
			featureTxt.setFont(RoboTextView.BOLD_FONT);
			featureTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			featureTxt.setPadding(0,0,0, (int) (6 * density));

			descriptionView.addView(featureTxt, params);
		}
	}

	private PlanConfig getDiamondConfig() {
		PlanConfig diamondConfig = new PlanConfig();
		diamondConfig.planDetailsBack = R.drawable.button_upgrade_diamond_default;
		diamondConfig.planIconId = R.drawable.ic_upgrade_diamond;
		diamondConfig.titleColor = getResources().getColor(R.color.upgrade_diamond_title);
		diamondConfig.titleId = R.string.diamond;
		diamondConfig.subTitleColor = getResources().getColor(R.color.upgrade_diamond_sub_title);
		diamondConfig.subTitleId = R.string.diamond_subtitle;
		diamondConfig.payViewColorId = R.color.diamond_back;
		diamondConfig.monthValue = DIAMOND_MONTH_PAY;
		diamondConfig.yearValue = DIAMOND_YEAR_PAY;
		diamondConfig.checkBoxDrawableId = R.drawable.button_checkmark_diamond_selector;
		diamondConfig.buttonBackId = R.drawable.button_orange_nb_selector;
		diamondConfig.descriptionBackId = R.drawable.button_upgrade_diamond_flat;
		diamondConfig.unlockFeaturesTitleId = R.string.upgrade_diamond_unlock_features_title;
		diamondConfig.features = getResources().getStringArray(R.array.upgrade_diamond_features);

		return diamondConfig;
	}

	private PlanConfig getPlatinumConfig() {
		PlanConfig platinumConfig = new PlanConfig();
		platinumConfig.planDetailsBack = R.drawable.button_upgrade_platinum_default;
		platinumConfig.planIconId = R.drawable.ic_upgrade_platinum;
		platinumConfig.titleColor = getResources().getColor(R.color.upgrade_platinum_title);
		platinumConfig.titleId = R.string.platinum;
		platinumConfig.subTitleColor = getResources().getColor(R.color.upgrade_platinum_sub_title);
		platinumConfig.subTitleId = R.string.platinum_subtitle;
		platinumConfig.payViewColorId = R.color.platinum_back;
		platinumConfig.monthValue = PLATINUM_MONTH_PAY;
		platinumConfig.yearValue = PLATINUM_YEAR_PAY;
		platinumConfig.checkBoxDrawableId = R.drawable.button_checkmark_platinum_selector;
		platinumConfig.buttonBackId = R.drawable.button_grey_solid_nb_selector;
		platinumConfig.descriptionBackId = R.drawable.button_upgrade_platinum_flat;
		platinumConfig.unlockFeaturesTitleId = R.string.upgrade_platinum_unlock_features_title;
		platinumConfig.features = getResources().getStringArray(R.array.upgrade_platinum_features);
		return platinumConfig;
	}

	private PlanConfig getGoldConfig() {
		PlanConfig goldConfig = new PlanConfig();
		goldConfig.planDetailsBack = R.drawable.button_upgrade_gold_default;
		goldConfig.planIconId = R.drawable.ic_upgrade_gold;
		goldConfig.titleColor = getResources().getColor(R.color.upgrade_gold_title);
		goldConfig.titleId = R.string.gold;
		goldConfig.subTitleColor = getResources().getColor(R.color.upgrade_gold_sub_title);
		goldConfig.subTitleId = R.string.gold_subtitle;
		goldConfig.payViewColorId = R.color.gold_back;
		goldConfig.monthValue = GOLD_MONTH_PAY;
		goldConfig.yearValue = GOLD_YEAR_PAY;
		goldConfig.checkBoxDrawableId = R.drawable.button_checkmark_gold_selector;
		goldConfig.buttonBackId = R.drawable.button_brown_solid_nb_selector;
		goldConfig.descriptionBackId = R.drawable.button_upgrade_gold_flat;
		goldConfig.unlockFeaturesTitleId = R.string.upgrade_gold_unlock_features_title;
		goldConfig.features = getResources().getStringArray(R.array.upgrade_gold_features);
		return goldConfig;
	}

	private int getIdForPlan() {
		int code = getArguments().getInt(PLAN);
		switch (code) {
			case PLATINUM:
				return R.id.platinumBtn;
			case GOLD:
				return R.id.goldBtn;
			case DIAMOND:
			default:
				return R.id.diamondBtn;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.monthCheckBox && isChecked) {
			yearCheckBox.setChecked(false);
		} else if (buttonView.getId() == R.id.yearCheckBox && isChecked) {
			monthCheckBox.setChecked(false);
		}
	}

	private class PlanConfig {
		int planDetailsBack;
		int planIconId;
		int titleColor;
		int titleId;
		int subTitleColor;
		int subTitleId;
		int payViewColorId;
		String monthValue;
		String yearValue;
		int checkBoxDrawableId;
		int descriptionBackId;
		int buttonBackId;
		int unlockFeaturesTitleId;
		String[] features;
	}
}
