package com.chess.ui.fragments.upgrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.billing.*;
import com.chess.backend.entity.api.MembershipItem;
import com.chess.backend.entity.api.MembershipKeyItem;
import com.chess.backend.entity.api.PayloadItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboTextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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

	protected static final String PLAN = "plan";

	protected static final String YEAR_DISCOUNT = "40%";
	public static final String GOLD_MONTHLY = "gold_monthly";
	public static final String GOLD_YEARLY = "gold_yearly";
	public static final String PLATINUM_MONTHLY = "platinum_monthly";
	public static final String PLATINUM_YEARLY = "platinum_yearly";
	public static final String DIAMOND_MONTHLY = "diamond_monthly";
	public static final String DIAMOND_YEARLY = "diamond_yearly";
	private static final int HASH_LENGTH = 88;
	public static final String PARAMS_DIVIDER = "||";

	public static final String UPGRADE_FROM_BASIC = "upgrade from basic";
	public static final String UPGRADE_FROM_GOLD = "upgrade from gold";
	public static final String UPGRADE_FROM_PLATINUM = "upgrade from platinum";
	public static final String UPGRADE_FROM_DIAMOND = "upgrade from diamond";

	public static final String UPGRADE_FROM_GOLD_MONTH = "upgrade from gold month";
	public static final String UPGRADE_FROM_GOLD_YEAR = "upgrade from gold year";
	public static final String UPGRADE_FROM_PLATINUM_MONTH = "upgrade from platinum month";
	public static final String UPGRADE_FROM_PLATINUM_YEAR = "upgrade from platinum year";
	public static final String UPGRADE_FROM_DIAMOND_MONTH = "upgrade from diamond month";
	public static final String AFFILIATION = "Android In-App Purchase";
	public static final String USD = "USD";
	public static final String PRODUCT_CATEGORY = "Membership upgrade";

	protected boolean isGoldMonthPayed;
	protected boolean isGoldYearPayed;
	protected boolean isPlatinumMonthPayed;
	protected boolean isPlatinumYearPayed;
	protected boolean isDiamondMonthPayed;
	protected boolean isDiamondYearPayed;

	private RadioGroup radioGroup;
	protected View planDetailsView;
	protected ImageView planImg;
	protected TextView planTitleTxt;
	protected TextView planSubTitleTxt;
	protected View monthView;
	protected TextView monthValueTxt;
	protected TextView monthLabelTxt;
	protected CheckBox monthCheckBox;
	protected View yearView;
	protected TextView yearValueTxt;
	protected TextView yearLabelTxt;
	protected CheckBox yearCheckBox;
	protected RoboButton setPlanBtn;
	protected LinearLayout descriptionView;

	protected PlanConfig[] configs;
	private IabHelper mHelper;
	private PayloadItem.Data payloadData;
	private GetDetailsListener detailsListener;
	private GetPayloadListener getPayloadListener;
	private String username;
	protected TextView yearDiscountTxt;
	protected int planCode;
	private View goldBtn;
	private View platinumBtn;
	protected int premiumStatus;
	private View disabledOverlayView;
	private int topPadding;

	public UpgradeDetailsFragment() {
	}

	public static UpgradeDetailsFragment createInstance(int code) {
		UpgradeDetailsFragment fragment = new UpgradeDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(PLAN, code);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			planCode = getArguments().getInt(PLAN);
		} else {
			planCode = savedInstanceState.getInt(PLAN);
		}

		configs = new PlanConfig[3];

		configs[DIAMOND] = PlanConfig.getDiamondConfig(getActivity());
		configs[PLATINUM] = PlanConfig.getPlatinumConfig(getActivity());
		configs[GOLD] = PlanConfig.getGoldConfig(getActivity());

		detailsListener = new GetDetailsListener();
		getPayloadListener = new GetPayloadListener();

		username = getAppData().getUsername();

		// get user membership level
		premiumStatus = getAppData().getUserPremiumStatus();
		topPadding = (int) (15 * density);

		// get key from server
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MEMBERSHIP_KEY);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<MembershipKeyItem>(new GetKeyListener()).executeTask(loadItem);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.upgrade_details_frame, container, false);
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

		setPlan();
	}

	protected void setPlan() {
		radioGroup.check(getIdForPlan());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(PLAN, planCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	// We're being destroyed. It's important to dispose of the helper here!
	@Override
	public void onDestroy() {
		super.onDestroy();

		// very important:
		if (mHelper != null) {
			mHelper.dispose();
			mHelper = null;
		}
	}

	protected void updateUiData() {
		// don't show lower upgrade options.
		if (premiumStatus == StaticData.DIAMOND_USER) { // disable platinum & gold
			platinumBtn.setEnabled(false);
			goldBtn.setEnabled(false);
			if (planCode == GOLD || planCode == PLATINUM) {
				disabledOverlayView.setVisibility(View.VISIBLE);
			} else {
				disabledOverlayView.setVisibility(View.GONE);
			}
		} else if (premiumStatus == StaticData.PLATINUM_USER) { // disable gold
			if (planCode == GOLD) {
				disabledOverlayView.setVisibility(View.VISIBLE);
			} else {
				disabledOverlayView.setVisibility(View.GONE);
			}
			goldBtn.setEnabled(false);
			platinumBtn.setEnabled(true);
		} else if (premiumStatus == StaticData.GOLD_USER) { // enable all
			disabledOverlayView.setVisibility(View.GONE);
			platinumBtn.setEnabled(true);
			goldBtn.setEnabled(true);
		}

		configs[GOLD].setMonthPayed(isGoldMonthPayed);
		configs[GOLD].setYearPayed(isGoldYearPayed);
		configs[PLATINUM].setMonthPayed(isPlatinumMonthPayed);
		configs[PLATINUM].setYearPayed(isPlatinumYearPayed);
		configs[DIAMOND].setMonthPayed(isDiamondMonthPayed);
		configs[DIAMOND].setYearPayed(isDiamondYearPayed);

		showPaymentPlan(configs[planCode]);
	}

	protected void showPaymentPlan(PlanConfig planConfig) {
		planDetailsView.setBackgroundResource(planConfig.planDetailsBack);
		if (premiumStatus == StaticData.DIAMOND_USER) {
			if (planCode == GOLD || planCode == PLATINUM) {
				planDetailsView.setPadding(0, 0, 0, 0);
			} else {
				if (!isTablet) {
					planDetailsView.setPadding(0, 0, 0, topPadding);
				}
			}
		} else if (premiumStatus == StaticData.PLATINUM_USER) {
			if (planCode == GOLD) {
				planDetailsView.setPadding(0, 0, 0, 0);
			} else {
				if (!isTablet) {
					planDetailsView.setPadding(0, 0, 0, topPadding);
				}
			}
		} else {
			if (!isTablet) {
				planDetailsView.setPadding(0, 0, 0, topPadding);
			}
		}

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
		monthCheckBox.setEnabled(!planConfig.isMonthPayed());
		// per year
		yearView.setBackgroundResource(planConfig.payViewColorId);
		yearView.setPadding(0, (int) (12 * density), 0, (int) (16 * density));

		yearValueTxt.setText(planConfig.yearValue);
		yearValueTxt.setTextColor(planConfig.titleColor);

		yearLabelTxt.setTextColor(planConfig.subTitleColor);
		yearCheckBox.setButtonDrawable(planConfig.checkBoxDrawableId);
		yearCheckBox.setEnabled(!planConfig.isYearPayed());

		showDiscountLabel(planConfig);

		setPlanBtn.setDrawableStyle(planConfig.buttonStyleId);
		descriptionView.setBackgroundResource(planConfig.descriptionBackId);
		int padding = (int) (15 * density);
		descriptionView.setPadding(padding, padding, padding, padding);

		descriptionView.removeAllViews();

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		RoboTextView unlockTitleTxt = new RoboTextView(getActivity());

		unlockTitleTxt.setTextColor(planConfig.subTitleColor);
		unlockTitleTxt.setFont(FontsHelper.ITALIC_FONT);
		unlockTitleTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		unlockTitleTxt.setPadding(0, 0, 0, (int) (7 * density));

		for (String feature : planConfig.features) {
			if (feature.trim().equals(Symbol.EMPTY)) {
				continue;
			}
			RoboTextView featureTxt = new RoboTextView(getActivity());
			featureTxt.setText(feature);
			featureTxt.setTextColor(planConfig.subTitleColor);
			featureTxt.setFont(FontsHelper.BOLD_FONT);
			featureTxt.setTextSize(14);
			featureTxt.setPadding(0, 0, 0, (int) (6 * density));

			descriptionView.addView(featureTxt, params);
		}
	}

	protected void showDiscountLabel(PlanConfig planConfig) {
		yearDiscountTxt.setVisibility(planConfig.isYearPayed() ? View.GONE : View.VISIBLE);
	}

	private int getIdForPlan() {
		switch (planCode) {
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
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.diamondBtn:
				planCode = DIAMOND;
				break;
			case R.id.platinumBtn:
				planCode = PLATINUM;
				break;
			case R.id.goldBtn:
				planCode = GOLD;
				break;
		}
		updateUiData();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.monthCheckBox && isChecked) {
			yearCheckBox.setChecked(false);
		} else if (buttonView.getId() == R.id.yearCheckBox && isChecked) {
			monthCheckBox.setChecked(false);
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.setPlanBtn) {
			if (premiumStatus == StaticData.DIAMOND_USER) { // disable platinum & gold
				if (planCode == GOLD || planCode == PLATINUM) {
					return;
				}
			} else if (premiumStatus == StaticData.PLATINUM_USER) { // disable gold
				if (planCode == GOLD) {
					return;
				}
			}

			boolean monthSubscription = monthCheckBox.isChecked();
			boolean yearSubscription = yearCheckBox.isChecked();
			if (!monthSubscription && !yearSubscription) {
				showToast(R.string.select_plan);
				return;
			}
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
				case R.id.goldBtn:
					if (monthSubscription) {
						sku = IabHelper.SKU_GOLD_MONTH;
					} else {
						sku = IabHelper.SKU_GOLD_YEAR;
					}
					break;
				default:
					showToast(R.string.select_plan);
					return;
			}
			sendPayment(sku);
		}
	}

	private class GetKeyListener extends ChessUpdateListener<MembershipKeyItem> {

		private GetKeyListener() {
			super(MembershipKeyItem.class);
		}

		@Override
		public void updateData(MembershipKeyItem returnedObj) {
			super.updateData(returnedObj);
			// we got key, start init
			setupIabHelper(returnedObj.getData());
		}
	}

	/**
	 * Set up IabHelper to get inventory of bought items and init data to be able launch purchase flow
	 */
	private void setupIabHelper(MembershipKeyItem.Data data) {
		mHelper = new IabHelper(getActivity(), data.getPublicKey());
		mHelper.enableDebugLogging(false);
		// get payload from server
		requestPayload(RestHelper.V_TRUE);
	}

	private void requestPayload(String isReload) {
		requestPayload(isReload, null);
	}

	private void requestPayload(String isReload, String sku) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MEMBERSHIP_PAYLOAD);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		if (sku != null) {
			loadItem.addRequestParams(RestHelper.P_PRODUCT_SKU, sku);
		}
		loadItem.addRequestParams(RestHelper.P_RELOAD, isReload);

		new RequestJsonTask<PayloadItem>(getPayloadListener).executeTask(loadItem);
	}

	// Listener that's called when we finish querying the items and subscriptions we own
	private class GotInventoryListener implements IabHelper.QueryInventoryFinishedListener {

		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			Activity activity = getActivity();
			if (isPaused || activity == null) {
				return;
			}

			setWaitScreen(false);
			if (result.isFailure()) {

				showSinglePopupDialog("Failed to query inventory: " + result);
				return;
			}

			if (!mHelper.subscriptionsSupported()) {
				showSinglePopupDialog("Subscriptions not supported on your device yet. Sorry!");
				return;
			}

            /*
			 * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See verifyDeveloperPayload().
             */

			// Check every purchased plan!
			{// gold month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_GOLD_MONTH);
				isGoldMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isGoldMonthPayed && !getAppData().getUserPremiumSku().equals(GOLD_MONTHLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}
			{// gold year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_GOLD_YEAR);
				isGoldYearPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isGoldYearPayed && !getAppData().getUserPremiumSku().equals(GOLD_YEARLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}
			{// platinum month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_PLATINUM_MONTH);
				isPlatinumMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isPlatinumMonthPayed && !getAppData().getUserPremiumSku().equals(PLATINUM_MONTHLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}
			{// platinum year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_PLATINUM_YEAR);
				isPlatinumYearPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isPlatinumYearPayed && !getAppData().getUserPremiumSku().equals(PLATINUM_YEARLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}
			{// diamond month
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_DIAMOND_MONTH);
				isDiamondMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isDiamondMonthPayed && !getAppData().getUserPremiumSku().equals(DIAMOND_MONTHLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}
			{// diamond year
				Purchase purchase = inventory.getPurchase(IabHelper.SKU_DIAMOND_YEAR);
				isDiamondYearPayed = purchase != null && verifyDeveloperPayload(purchase);
				if (isDiamondYearPayed && !getAppData().getUserPremiumSku().equals(DIAMOND_YEARLY)) {
					updateMembershipOnServer(purchase);
					return;
				}
			}

			// query our server for membership bought from non Google Play( Apple, Web)
			LoadItem loadItem = LoadHelper.getMembershipDetails(getUserToken());
			new RequestJsonTask<MembershipItem>(detailsListener).executeTask(loadItem);
		}
	}

	private class GetDetailsListener extends ChessUpdateListener<MembershipItem> {

		private GetDetailsListener() {
			super(MembershipItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			setWaitScreen(show);
		}

		@Override
		public void updateData(MembershipItem returnedObj) {
			super.updateData(returnedObj);

			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			premiumStatus = returnedObj.getData().getLevel();
			getAppData().setUserPremiumStatus(premiumStatus);

			// update selected modes
			if (returnedObj.getData().getIs_premium() > 0) {

				String sku = returnedObj.getData().getSku();

				isGoldMonthPayed = sku.equals(GOLD_MONTHLY);
				if (isGoldMonthPayed) {
					getAppData().setUserPremiumSku(GOLD_MONTHLY);
				}
				isGoldYearPayed = sku.equals(GOLD_YEARLY);
				if (isGoldYearPayed) {
					getAppData().setUserPremiumSku(GOLD_YEARLY);
				}
				isPlatinumMonthPayed = sku.equals(PLATINUM_MONTHLY);
				if (isPlatinumMonthPayed) {
					getAppData().setUserPremiumSku(PLATINUM_MONTHLY);
				}
				isPlatinumYearPayed = sku.equals(PLATINUM_YEARLY);
				if (isPlatinumYearPayed) {
					getAppData().setUserPremiumSku(PLATINUM_YEARLY);
				}
				isDiamondMonthPayed = sku.equals(DIAMOND_MONTHLY);
				if (isDiamondMonthPayed) {
					getAppData().setUserPremiumSku(DIAMOND_MONTHLY);
				}
				isDiamondYearPayed = sku.equals(DIAMOND_YEARLY);
				if (isDiamondYearPayed) {
					getAppData().setUserPremiumSku(DIAMOND_YEARLY);
				}
			}

			updateUiData();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			// show message only for re-login
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.INVALID_ORDER) {
					LoadItem loadItem = LoadHelper.getMembershipDetails(getUserToken());
					new RequestJsonTask<MembershipItem>(detailsListener).executeTask(loadItem);
				} else {
					super.errorHandle(resultCode);
				}
			}
		}
	}

	private void updateMembershipOnServer(Purchase purchase) {
		if (purchase == null) {
			return;
		}

		trackPurchaseToGA(purchase);

		LoadItem loadItem = LoadHelper.postMembershipUpdate(getUserToken(), purchase.getOriginalJson(), purchase.getSignature());
		new RequestJsonTask<MembershipItem>(detailsListener).executeTask(loadItem);
	}

	private void trackPurchaseToGA(Purchase purchase) {
		EasyTracker easyTracker = getActivityFace().getGATracker();

		if (easyTracker == null) {
			return;
		}
		String sku = purchase.getSku();


		AppData appData = getAppData();
		String userPreviousSku = appData.getUserPremiumSku();
		int userPremiumStatus = appData.getUserPremiumStatus();
		String productName = Symbol.EMPTY;

		if (TextUtils.isEmpty(userPreviousSku)) {
			if (userPremiumStatus == StaticData.BASIC_USER) {
				productName = UPGRADE_FROM_BASIC;
			} else if (userPremiumStatus == StaticData.GOLD_USER) {
				productName = UPGRADE_FROM_GOLD;
			} else if (userPremiumStatus == StaticData.PLATINUM_USER) {
				productName = UPGRADE_FROM_PLATINUM;
			} else if (userPremiumStatus == StaticData.DIAMOND_USER) {
				productName = UPGRADE_FROM_DIAMOND;
			}
		} else {
			if (userPreviousSku.equals(IabHelper.SKU_GOLD_MONTH)) {
				productName = UPGRADE_FROM_GOLD_MONTH;
			} else if (userPreviousSku.equals(IabHelper.SKU_GOLD_YEAR)) {
				productName = UPGRADE_FROM_GOLD_YEAR;
			} else if (userPreviousSku.equals(IabHelper.SKU_PLATINUM_MONTH)) {
				productName = UPGRADE_FROM_PLATINUM_MONTH;
			} else if (userPreviousSku.equals(IabHelper.SKU_PLATINUM_YEAR)) {
				productName = UPGRADE_FROM_PLATINUM_YEAR;
			} else if (userPreviousSku.equals(IabHelper.SKU_DIAMOND_MONTH)) {
				productName = UPGRADE_FROM_DIAMOND_MONTH;
			} else if (userPreviousSku.equals(IabHelper.SKU_DIAMOND_YEAR)) {
				productName = UPGRADE_FROM_GOLD_MONTH;
			}
		}

		double revenue = 0;
		if (sku.equals(GOLD_MONTHLY)) {
			revenue = Integer.parseInt(PlanConfig.GOLD_MONTH_PAY.substring(1));
		} else if (sku.equals(GOLD_YEARLY)) {
			revenue = Integer.parseInt(PlanConfig.GOLD_YEAR_PAY.substring(1));
		} else if (sku.equals(PLATINUM_MONTHLY)) {
			revenue = Integer.parseInt(PlanConfig.PLATINUM_MONTH_PAY.substring(1));
		} else if (sku.equals(PLATINUM_YEARLY)) {
			revenue = Integer.parseInt(PlanConfig.PLATINUM_YEAR_PAY.substring(1));
		} else if (sku.equals(DIAMOND_MONTHLY)) {
			revenue = Integer.parseInt(PlanConfig.DIAMOND_MONTH_PAY.substring(1));
		} else if (sku.equals(DIAMOND_YEARLY)) {
			revenue = Integer.parseInt(PlanConfig.DIAMOND_YEAR_PAY.substring(1));
		}

		String transactionId = purchase.getOrderId();
		String affiliation = AFFILIATION;
		String productCategory = PRODUCT_CATEGORY;

		double tax = 0.0d;
		double shipping = 0.0d;
		String currencyCode = USD;

		Map<String, String> gaTransaction = MapBuilder
				.createTransaction(
						transactionId,  // (String) Transaction ID
						affiliation,    // (String) Affiliation
						revenue,        // (Double) Order revenue
						tax,            // (Double) Tax
						shipping,       // (Double) Shipping
						currencyCode)   // (String) Currency code
				.build();
		easyTracker.send(gaTransaction);

		Map<String, String> gaItem = MapBuilder
				.createItem(
						transactionId,   // (String) Transaction ID
						productName,     // (String) Product name
						sku,             // (String) Product SKU
						productCategory, // (String) Product category
						revenue,         // (Double) Product price
						1L,              // (Long) Product quantity
						currencyCode)    // (String) Currency code
				.build();

		easyTracker.send(gaItem);
	}

	private class GetPayloadListener extends ChessUpdateListener<PayloadItem> {

		private GetPayloadListener() {
			super(PayloadItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			setWaitScreen(show);
		}

		@Override
		public void updateData(PayloadItem returnedObj) {
			super.updateData(returnedObj);

			payloadData = returnedObj.getData();

			mHelper.startSetup(new IabSetupFinishedListener());
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.USER_DONT_HAVE_VALID_PAYLOAD) {
					requestPayload(RestHelper.V_FALSE);
					return;
				}

			}
			super.errorHandle(resultCode);
		}
	}

	protected void sendPayment(String itemId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_MEMBERSHIP_PAYLOAD);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_PRODUCT_SKU, itemId);
		loadItem.addRequestParams(RestHelper.P_RELOAD, RestHelper.V_TRUE);

		new RequestJsonTask<PayloadItem>(new GetPayloadListener1(itemId)).executeTask(loadItem);
	}

	private class GetPayloadListener1 extends ChessUpdateListener<PayloadItem> {

		private String itemId;

		private GetPayloadListener1(String itemId) {
			super(PayloadItem.class);
			this.itemId = itemId;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			setWaitScreen(show);
		}

		@Override
		public void updateData(PayloadItem returnedObj) {
			super.updateData(returnedObj);

			payloadData = returnedObj.getData();

			// cut previous additional params
			String usernameEncoded = Base64.encode((username + PARAMS_DIVIDER + itemId).getBytes());

			String load = payloadData.getPayload().substring(0, HASH_LENGTH);

			String purchasePayload = load + usernameEncoded;

//			{// Test part
//				String serverPayLoad = payloadData.getPayload();
//				String userNameSku = purchasePayload.substring(HASH_LENGTH);
//				String purchasePayload1 = purchasePayload.substring(0, HASH_LENGTH);
//				String serverPayLoad1 = serverPayLoad.substring(0, HASH_LENGTH);
//				try {
//					byte[] decode = Base64.decode(userNameSku);
//					userNameSku = new String(decode, "UTF-8");
//				} catch (Base64DecoderException e) {
//					e.printStackTrace();
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//
//				String[] purchaseParams = userNameSku.split("[||]");
//				if (purchaseParams.length == 3) {
//					String purchaseUserName = purchaseParams[0];
//					String purchaseSku = purchaseParams[2];
//					logTest(purchaseUserName);
//					logTest(purchaseSku);
//				}
//
//				logTest(purchasePayload1);
//				logTest(serverPayLoad1);
//			}
			setWaitScreen(true);
			mHelper.launchPurchaseFlow(getActivity(), itemId, IabHelper.ITEM_TYPE_SUBS,
					RC_REQUEST, new PurchaseFinishedListener(), purchasePayload);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.USER_DONT_HAVE_VALID_PAYLOAD) {
					requestPayload(RestHelper.V_FALSE);
					return;
				}
			}
			super.errorHandle(resultCode);
		}
	}

	// Callback for when a purchase is finished
	private class PurchaseFinishedListener implements IabHelper.OnIabPurchaseFinishedListener {

		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			if (getActivity() == null) {
				logTest("onIabPurchaseFinished - >activity null");
				return;
			}
			setWaitScreen(false);

			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
			// skip if user canceled
			if (result.isFailure() && result.getResponse() != IabHelper.IABHELPER_USER_CANCELLED) {
				showSinglePopupDialog("Error purchasing: " + result);

				return;
			} else if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED || purchase == null) {
				// nothing happened
				return;
			}

			if (!verifyDeveloperPayload(purchase)) {
				showSinglePopupDialog("Authenticity verification failed.");
				logTest("oops - user =" + username
						+ " order = " + purchase.getSku() + "payload = " + purchase.getDeveloperPayload()
						+ " real payload = " + payloadData.getPayload());
				return;
			}

			updateMembershipOnServer(purchase);
		}
	}

	private class IabSetupFinishedListener implements IabHelper.OnIabSetupFinishedListener {
		@Override
		public void onIabSetupFinished(IabResult result) {
			if (getActivity() == null) {
				return;
			}

			if (!result.isSuccess()) {
				// Oh noes, there was a problem.
				showSinglePopupDialog("Problem setting up in-app billing: " + result);
				return;
			}

			// Hooray, IAB is fully set up. Now, let's get an inventory of stuff we own.
			mHelper.queryInventoryAsync(new GotInventoryListener());
			setWaitScreen(true);
		}
	}

	/**
	 * Verifies the developer payload of a purchase with the one that comes with purchase
	 */
	boolean verifyDeveloperPayload(Purchase purchase) {
		String purchasePayload = purchase.getDeveloperPayload();
		if (payloadData == null) {
			return false;
		}
		// disassemble purchase data and compare every field
		String sku = purchase.getSku();
		String serverPayLoad = payloadData.getPayload();
		String userNameSku = purchasePayload.substring(HASH_LENGTH);
		purchasePayload = purchasePayload.substring(0, HASH_LENGTH);
		serverPayLoad = serverPayLoad.substring(0, HASH_LENGTH);
		try {
			byte[] decode = Base64.decode(userNameSku);
			userNameSku = new String(decode, "UTF-8");
		} catch (Base64DecoderException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		String[] purchaseParams = userNameSku.split("[||]");
		if (purchaseParams.length == 3) {
			String purchaseUserName = purchaseParams[0];
			String purchaseSku = purchaseParams[2];
			return serverPayLoad.equals(purchasePayload) && purchaseUserName.equals(username) && purchaseSku.equals(sku);
		} else {
			return false;
		}
	}

	private void setWaitScreen(boolean show) {
		showLoadingProgress(show);
	}

	protected void widgetsInit(View view) {
		radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(this);

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

		platinumBtn = view.findViewById(R.id.platinumBtn);
		goldBtn = view.findViewById(R.id.goldBtn);

		disabledOverlayView = view.findViewById(R.id.disableOverlayView);
	}

	static class PlanConfig {

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
			goldConfig.features = resources.getStringArray(R.array.upgrade_gold_features);
			return goldConfig;
		}


	}
}
