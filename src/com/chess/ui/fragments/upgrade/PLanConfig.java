package com.chess.ui.fragments.upgrade;

import android.content.Context;
import android.content.res.Resources;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.05.13
 * Time: 14:06
 */
public class PlanConfig {

	public static final String DIAMOND_MONTH_PAY = "$14";
	public static final String DIAMOND_YEAR_PAY = "$99";
	public static final String PLATINUM_MONTH_PAY = "$7";
	public static final String PLATINUM_YEAR_PAY = "$49";
	public static final String GOLD_MONTH_PAY = "$5";
	public static final String GOLD_YEAR_PAY = "$29";

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
	int buttonStyleId;
	int unlockFeaturesTitleId;
	String[] features;
	private boolean yearPayed;
	private boolean monthPayed;

	public boolean isYearPayed() {
		return yearPayed;
	}

	public void setYearPayed(boolean yearPayed) {
		this.yearPayed = yearPayed;
	}

	public boolean isMonthPayed() {
		return monthPayed;
	}

	public void setMonthPayed(boolean monthPayed) {
		this.monthPayed = monthPayed;
	}

	static PlanConfig getDiamondConfig(Context context) {
		Resources resources = context.getResources();

		PlanConfig diamondConfig = new PlanConfig();
		diamondConfig.planDetailsBack = R.drawable.button_upgrade_diamond_default;
		diamondConfig.planIconId = R.drawable.ic_upgrade_diamond;
		diamondConfig.titleColor = resources.getColor(R.color.upgrade_diamond_title);
		diamondConfig.titleId = R.string.diamond;
		diamondConfig.subTitleColor = resources.getColor(R.color.upgrade_diamond_sub_title);
		diamondConfig.subTitleId = R.string.diamond_subtitle;
		diamondConfig.payViewColorId = R.color.diamond_back;
		diamondConfig.monthValue = DIAMOND_MONTH_PAY;
		diamondConfig.yearValue = DIAMOND_YEAR_PAY;
		diamondConfig.checkBoxDrawableId = R.drawable.button_checkmark_diamond_selector;
		diamondConfig.buttonStyleId = R.style.Button_Blue;
		diamondConfig.descriptionBackId = R.drawable.button_upgrade_diamond_flat;
		diamondConfig.unlockFeaturesTitleId = R.string.upgrade_diamond_unlock_features_title;
		diamondConfig.features = resources.getStringArray(R.array.upgrade_diamond_features);

		return diamondConfig;
	}

	static PlanConfig getPlatinumConfig(Context context) {
		Resources resources = context.getResources();

		PlanConfig platinumConfig = new PlanConfig();
		platinumConfig.planDetailsBack = R.drawable.button_upgrade_platinum_default;
		platinumConfig.planIconId = R.drawable.ic_upgrade_platinum;
		platinumConfig.titleColor = resources.getColor(R.color.upgrade_platinum_title);
		platinumConfig.titleId = R.string.platinum;
		platinumConfig.subTitleColor = resources.getColor(R.color.upgrade_platinum_sub_title);
		platinumConfig.subTitleId = R.string.platinum_subtitle;
		platinumConfig.payViewColorId = R.color.platinum_back;
		platinumConfig.monthValue = PLATINUM_MONTH_PAY;
		platinumConfig.yearValue = PLATINUM_YEAR_PAY;
		platinumConfig.checkBoxDrawableId = R.drawable.button_checkmark_platinum_selector;
		platinumConfig.buttonStyleId = R.style.Button_Grey2Solid_NoBorder;
		platinumConfig.descriptionBackId = R.drawable.button_upgrade_platinum_flat;
		platinumConfig.unlockFeaturesTitleId = R.string.upgrade_platinum_unlock_features_title;
		platinumConfig.features = resources.getStringArray(R.array.upgrade_platinum_features);
		return platinumConfig;
	}

	static PlanConfig getGoldConfig(Context context) {
		Resources resources = context.getResources();

		PlanConfig goldConfig = new PlanConfig();
		goldConfig.planDetailsBack = R.drawable.button_upgrade_gold_default;
		goldConfig.planIconId = R.drawable.ic_upgrade_gold;
		goldConfig.titleColor = resources.getColor(R.color.upgrade_gold_title);
		goldConfig.titleId = R.string.gold;
		goldConfig.subTitleColor = resources.getColor(R.color.upgrade_gold_sub_title);
		goldConfig.subTitleId = R.string.gold_subtitle;
		goldConfig.payViewColorId = R.color.gold_back;
		goldConfig.monthValue = GOLD_MONTH_PAY;
		goldConfig.yearValue = GOLD_YEAR_PAY;
		goldConfig.checkBoxDrawableId = R.drawable.button_checkmark_gold_selector;
		goldConfig.buttonStyleId = R.style.Button_Brown;
		goldConfig.descriptionBackId = R.drawable.button_upgrade_gold_flat;
		goldConfig.unlockFeaturesTitleId = R.string.upgrade_gold_unlock_features_title;
		goldConfig.features = resources.getStringArray(R.array.upgrade_gold_features);
		return goldConfig;
	}


}
