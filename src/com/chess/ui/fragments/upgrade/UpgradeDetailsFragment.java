package com.chess.ui.fragments.upgrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.billing.IabHelper;
import com.chess.backend.billing.IabResult;
import com.chess.backend.billing.Inventory;
import com.chess.backend.billing.Purchase;
import com.chess.backend.statics.StaticData;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.02.13
 * Time: 18:45
 */
public class UpgradeDetailsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener,
		CompoundButton.OnCheckedChangeListener {

	private static final String TAG = "UpgradeDetailsFragment";

	public static final int RC_REQUEST = 10001;

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
	private IabHelper mHelper;

	public static UpgradeDetailsFragment newInstance(int code){
		UpgradeDetailsFragment frag = new UpgradeDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(PLAN, code);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		 /* base64EncodedPublicKey should be YOUR APPLICATION'S PUBLIC KEY
         * (that you got from the Google Play developer console). This is not your
         * developer public key, it's the *app-specific* public key.
         *
         * Instead of just storing the entire literal string here embedded in the
         * program,  construct the key at runtime from pieces or
         * use bit manipulation (for example, XOR with some other string) to hide
         * the actual key.  The key itself is not secret information, but we don't
         * want to make it easy for an attacker to replace the public key with one
         * of their own and then fake messages from the server.
         */
		// TODO get public key from server
		String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkCrfcT7Ho29ms/a8zvzWvgtcsDmI/gKFnNlFmtNGREGvfquFSzwz7SjisHX6/Lq3//d1lVWxrRpsQcUwfkOYHshVbym5mzaW/06UgADPa4eRJ+JK4+1ts+oJJGX0eJYMhWP16ppQ1NVFSAWyE0WHhyDis1dIyCLZxKIVcZP9aKeAoVM94QPOdv5MU2O61tzEnEID1KXq9Wr8raKngJDZ2EcUn+1oLilRCp7gFbfU8jlZd0326WA5cLOoAVJQ6EV2WnDNmGWBgcMYOT3WBoHWWZjRyPPL3VWsnEkA0CHFS3RCNzBADPj9UPIwaXINhCoLC+NXiXUuEC0V/bD0O3QYWwIDAQAB";

		// Create the helper, passing it our context and the public key to verify signatures with
		Log.d(TAG, "Creating IAB helper.");
		mHelper = new IabHelper(getActivity(), base64EncodedPublicKey);

		// enable debug logging (for a production application, you should set this to false).
		mHelper.enableDebugLogging(true);

		// Start setup. This is asynchronous and the specified listener
		// will be called once setup completes.
		Log.d(TAG, "Starting setup.");
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				Log.d(TAG, "Setup finished.");

				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
					showSinglePopupDialog("Problem setting up in-app billing: " + result);
					return;
				}

				// Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
				Log.d(TAG, "Setup successful. Querying inventory.");
				mHelper.queryInventoryAsync(mGotInventoryListener);
			}
		});
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
		setPlanBtn.setOnClickListener(this);
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
		configs[GOLD].setMonthPayed(isGoldMonthPayed);
		configs[GOLD].setYearPayed(isGoldYearPayed);
		configs[PLATINUM].setMonthPayed(isPlatinumMonthPayed);
		configs[PLATINUM].setYearPayed(isPlatinumYearPayed);
		configs[DIAMOND].setMonthPayed(isDiamondMonthPayed);
		configs[DIAMOND].setYearPayed(isDiamondYearPayed);

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
//		monthCheckBox.setChecked(planConfig.isMonthPayed()); //TODO adjust
		monthCheckBox.setEnabled(!planConfig.isMonthPayed());
		// per year
		yearView.setBackgroundResource(planConfig.payViewColorId);
		yearView.setPadding(0, (int) (12 * density), 0, (int) (16 * density));

		yearValueTxt.setText(planConfig.yearValue);
		yearValueTxt.setTextColor(planConfig.titleColor);

		yearLabelTxt.setTextColor(planConfig.subTitleColor);
		yearCheckBox.setButtonDrawable(planConfig.checkBoxDrawableId);
//		yearCheckBox.setChecked(planConfig.isYearPayed());
		yearCheckBox.setEnabled(!planConfig.isYearPayed());

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
		unlockTitleTxt.setPadding(0, 0, 0, (int) (7 * density));
		descriptionView.addView(unlockTitleTxt, params);

		for (String feature : planConfig.features) {
			RoboTextView featureTxt = new RoboTextView(getActivity());
			featureTxt.setText(feature);
			featureTxt.setTextColor(planConfig.subTitleColor);
			featureTxt.setFont(RoboTextView.BOLD_FONT);
			featureTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			featureTxt.setPadding(0, 0, 0, (int) (6 * density));

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
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		}
		else {
			Log.d(TAG, "onActivityResult handled by IABUtil.");
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.setPlanBtn) {
			boolean monthSubscription = monthCheckBox.isChecked();
			String sku;
			switch (radioGroup.getCheckedRadioButtonId()) {
				case R.id.diamondBtn:
					if (monthSubscription) {
						sku = IabHelper.SKU_DIAMOND_MONTH;
					} else {
						sku = IabHelper.SKU_DIAMOND_YEAR;
					}
					break;
				case R.id.platinumBtn:
					if (monthSubscription) {
						sku = IabHelper.SKU_PLATINUM_MONTH;
					} else {
						sku = IabHelper.SKU_PLATINUM_YEAR;
					}
					break;
				default:
				case R.id.goldBtn:
					if (monthSubscription) {
						sku = IabHelper.SKU_GOLD_MONTH;
					} else {
						sku = IabHelper.SKU_GOLD_YEAR;
					}
					break;
			}
			sendPaymentRequest(sku);
		}
	}

	private void sendPaymentRequest(String itemId) {
		if (!mHelper.subscriptionsSupported()) {
			showSinglePopupDialog("Subscriptions not supported on your device yet. Sorry!");
			return;
		}

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
		String payload = "";

		setWaitScreen(true);
		Log.d(TAG, "Launching purchase flow for infinite gas subscription.");
		mHelper.launchPurchaseFlow(getActivity(), itemId, IabHelper.ITEM_TYPE_SUBS,
				RC_REQUEST, mPurchaseFinishedListener, payload);
	}

	// Callback for when a purchase is finished
	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
			if (result.isFailure()) {
				showSinglePopupDialog("Error purchasing: " + result);
				setWaitScreen(false);
				return;
			}
			if (!verifyDeveloperPayload(purchase)) {
				showSinglePopupDialog("Error purchasing. Authenticity verification failed.");
				setWaitScreen(false);
				return;
			}

			Log.d(TAG, "Purchase successful.");

			if (purchase.getSku().equals(IabHelper.SKU_GOLD_MONTH)) {
				// bought the infinite gas subscription
				Log.d(TAG, "Infinite gas subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_GOLD_MONTH);
				isGoldMonthPayed = true;
				updateData();
				setWaitScreen(false);
			} else if (purchase.getSku().equals(IabHelper.SKU_GOLD_YEAR)) {
				Log.d(TAG, IabHelper.SKU_GOLD_YEAR  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_GOLD_YEAR);
				isGoldYearPayed = true;
			} else if (purchase.getSku().equals(IabHelper.SKU_PLATINUM_MONTH)) {
				Log.d(TAG, IabHelper.SKU_PLATINUM_MONTH  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_PLATINUM_MONTH);
				isPlatinumMonthPayed = true;
			} else if (purchase.getSku().equals(IabHelper.SKU_PLATINUM_YEAR)) {
				Log.d(TAG, IabHelper.SKU_PLATINUM_YEAR  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_PLATINUM_YEAR);
				isPlatinumYearPayed = true;
			} else if (purchase.getSku().equals(IabHelper.SKU_DIAMOND_MONTH)) {
				Log.d(TAG, IabHelper.SKU_DIAMOND_MONTH  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_DIAMOND_MONTH);
				isDiamondMonthPayed = true;
			} else if (purchase.getSku().equals(IabHelper.SKU_DIAMOND_YEAR)) {
				Log.d(TAG, IabHelper.SKU_DIAMOND_YEAR  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_DIAMOND_YEAR);
				isDiamondYearPayed = true;
			}
			updateData();
			setWaitScreen(false);
		}
	};

	// We're being destroyed. It's important to dispose of the helper here!
	@Override
	public void onDestroy() {
		super.onDestroy();

		// very important:
		Log.d(TAG, "Destroying helper.");
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
	}

	private boolean isGoldMonthPayed;
	private boolean isGoldYearPayed;
	private boolean isPlatinumMonthPayed;
	private boolean isPlatinumYearPayed;
	private boolean isDiamondMonthPayed;
	private boolean isDiamondYearPayed;

	// Listener that's called when we finish querying the items and subscriptions we own
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Log.d(TAG, "Query inventory finished.");
			if (result.isFailure()) {
				showSinglePopupDialog("Failed to query inventory: " + result);
				return;
			}

			Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

			// Check every purchased plan?
			{// gold month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_GOLD_MONTH);

//				isGoldMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
				isGoldMonthPayed = true; // TODO restore
			}
			{// gold year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_GOLD_YEAR);

				isGoldYearPayed = purchase != null && verifyDeveloperPayload(purchase);
			}
			{// platinum month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_PLATINUM_MONTH);

				isPlatinumMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
			}
			{// platinum year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_PLATINUM_YEAR);

				isPlatinumYearPayed = purchase != null && verifyDeveloperPayload(purchase);
			}
			{// diamond month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_DIAMOND_MONTH);

				isDiamondMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
			}
			{// diamond year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_DIAMOND_YEAR);

				isDiamondYearPayed = purchase != null && verifyDeveloperPayload(purchase);
			}


			updateData();

			setWaitScreen(false);
			Log.d(TAG, "Initial inventory query finished; enabling main UI.");
		}
	};

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();
		// TODO get payload from server
        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

		return true;
	}

	private void setWaitScreen(boolean show) {
		if (show){
			showPopupProgressDialog(R.string.processing_);
		} else {
//			if(isPaused)
//				return;

			dismissProgressDialog();
		}
	}
}
