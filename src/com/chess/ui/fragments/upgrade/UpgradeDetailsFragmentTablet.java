package com.chess.ui.fragments.upgrade;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.billing.IabHelper;
import com.chess.statics.Symbol;
import com.chess.widgets.RoboButton;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.11.13
 * Time: 14:11
 */
public class UpgradeDetailsFragmentTablet extends UpgradeDetailsFragment {

	public UpgradeDetailsFragmentTablet() {
	}

	public static UpgradeDetailsFragmentTablet createInstance(int code) {
		UpgradeDetailsFragmentTablet fragment = new UpgradeDetailsFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putInt(PLAN, code);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void setPlan() {
		updateUiData();
	}

	@Override
	protected void updateUiData() {
		configs[GOLD].setMonthPayed(isGoldMonthPayed);
		configs[GOLD].setYearPayed(isGoldYearPayed);
		configs[PLATINUM].setMonthPayed(isPlatinumMonthPayed);
		configs[PLATINUM].setYearPayed(isPlatinumYearPayed);
		configs[DIAMOND].setMonthPayed(isDiamondMonthPayed);
		configs[DIAMOND].setYearPayed(isDiamondYearPayed);

		switch (planCode) {
			case DIAMOND:
				showPaymentPlan(configs[DIAMOND]);
				break;
			case PLATINUM:
				showPaymentPlan(configs[PLATINUM]);
				break;
			case GOLD:
				showPaymentPlan(configs[GOLD]);
				break;
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.setPlanBtn) {
			boolean monthSubscription = monthCheckBox.isChecked();
			String sku;
			switch (planCode) {
				case DIAMOND:
					if (monthSubscription) {
						sku = IabHelper.SKU_DIAMOND_MONTH;
					} else {
						sku = IabHelper.SKU_DIAMOND_YEAR;
					}
					break;
				case PLATINUM:
					if (monthSubscription) {
						sku = IabHelper.SKU_PLATINUM_MONTH;
					} else {
						sku = IabHelper.SKU_PLATINUM_YEAR;
					}
					break;
				default:
				case GOLD:
					if (monthSubscription) {
						sku = IabHelper.SKU_GOLD_MONTH;
					} else {
						sku = IabHelper.SKU_GOLD_YEAR;
					}
					break;
			}
			sendPayment(sku);
		}
	}

	@Override
	protected void showDiscountLabel(PlanConfig planConfig) {
		if (!planConfig.isYearPayed() && planCode == DIAMOND) {
			yearDiscountTxt.setVisibility(View.VISIBLE);
		} else {
			yearDiscountTxt.setVisibility(View.GONE);
		}
	}

	@Override
	protected void widgetsInit(View view) {
		planDetailsView = view.findViewById(R.id.planDetailsView);
		planImg = (ImageView) view.findViewById(R.id.planImg);
		planTitleTxt = (TextView) view.findViewById(R.id.planTitleTxt);
		planSubTitleTxt = (TextView) view.findViewById(R.id.planSubTitleTxt);
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
		yearDiscountTxt = (TextView) view.findViewById(R.id.yearDiscountTxt);
		setPlanBtn = (RoboButton) view.findViewById(R.id.setPlanBtn);
		setPlanBtn.setOnClickListener(this);
		descriptionView = (LinearLayout) view.findViewById(R.id.descriptionView);

		yearDiscountTxt.setText(getString(R.string.save) + Symbol.NEW_STR + YEAR_DISCOUNT);

		if (planCode == DIAMOND) {
			yearDiscountTxt.setVisibility(View.VISIBLE);
		} else {
			yearDiscountTxt.setVisibility(View.GONE);
		}
	}

}
