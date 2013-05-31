package com.chess.ui.fragments.upgrade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.backend.RestHelper;
import com.chess.backend.billing.IabHelper;
import com.chess.backend.billing.IabResult;
import com.chess.backend.billing.Inventory;
import com.chess.backend.billing.Purchase;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.MembershipItem;
import com.chess.backend.entity.new_api.MembershipKeyItem;
import com.chess.backend.entity.new_api.PayloadItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
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

	private static final String YEAR_DISCOUNT = "40%";

	private boolean isGoldMonthPayed;
	private boolean isGoldYearPayed;
	private boolean isPlatinumMonthPayed;
	private boolean isPlatinumYearPayed;
	private boolean isDiamondMonthPayed;
	private boolean isDiamondYearPayed;

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
	private RoboButton setPlanBtn;
	private LinearLayout descriptionView;

	private float density;
	private PlanConfig[] configs;
	private IabHelper mHelper;
	private PayloadItem.Data payloadData;
	private GetDetailsListener detailsListener;

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

		detailsListener = new GetDetailsListener();

		// get key from server
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP_KEY);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

		new RequestJsonTask<MembershipKeyItem>(new GetKeyListener()).executeTask(loadItem); // TODO set proper item
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
		setPlanBtn = (RoboButton) view.findViewById(R.id.setPlanBtn);
		setPlanBtn.setOnClickListener(this);
		descriptionView = (LinearLayout) view.findViewById(R.id.descriptionView);

		yearDiscountTxt.setText(getString(R.string.save) + StaticData.SYMBOL_NEW_STR + YEAR_DISCOUNT);

		configs = new PlanConfig[3];

		configs[0] = PlanConfig.getDiamondConfig(getActivity());
		configs[1] = PlanConfig.getPlatinumConfig(getActivity());
		configs[2] = PlanConfig.getGoldConfig(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		radioGroup.check(getIdForPlan());
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

	// We're being destroyed. It's important to dispose of the helper here!
	@Override
	public void onDestroy() {
		super.onDestroy();

		// very important:
		Log.d(TAG, "Destroying helper.");
		if (mHelper != null) mHelper.dispose();
		mHelper = null;
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
		monthCheckBox.setEnabled(!planConfig.isMonthPayed());
		// per year
		yearView.setBackgroundResource(planConfig.payViewColorId);
		yearView.setPadding(0, (int) (12 * density), 0, (int) (16 * density)); // TODO improve performance

		yearValueTxt.setText(planConfig.yearValue);
		yearValueTxt.setTextColor(planConfig.titleColor);

		yearLabelTxt.setTextColor(planConfig.subTitleColor);
		yearCheckBox.setButtonDrawable(planConfig.checkBoxDrawableId);
		yearCheckBox.setEnabled(!planConfig.isYearPayed());

		setPlanBtn.setDrawableStyle(planConfig.buttonStyleId);
		descriptionView.setBackgroundResource(planConfig.descriptionBackId);
		int padding = (int) (15 * density);
		descriptionView.setPadding(padding, padding, padding, padding);

		descriptionView.removeAllViews();
		// TODO improve!
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		RoboTextView unlockTitleTxt = new RoboTextView(getActivity());

		unlockTitleTxt.setTextColor(planConfig.subTitleColor);
		unlockTitleTxt.setText(planConfig.unlockFeaturesTitleId);
		unlockTitleTxt.setFont(FontsHelper.ITALIC_FONT);
		unlockTitleTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
		unlockTitleTxt.setPadding(0, 0, 0, (int) (7 * density));
		descriptionView.addView(unlockTitleTxt, params);

		for (String feature : planConfig.features) {
			RoboTextView featureTxt = new RoboTextView(getActivity());
			featureTxt.setText(feature);
			featureTxt.setTextColor(planConfig.subTitleColor);
			featureTxt.setFont(FontsHelper.BOLD_FONT);
			featureTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
			featureTxt.setPadding(0, 0, 0, (int) (6 * density));    // TODO remove hardcode

			descriptionView.addView(featureTxt, params);
		}
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

//		@Override
//		public void errorHandle(Integer resultCode) {
//			MembershipKeyItem item = new MembershipKeyItem();
//			MembershipKeyItem.Data data = new MembershipKeyItem.Data();
//			data.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8PdNZ3V+FkqhBry7E2zAKx9Tvj/wcCBNs7zOm/dZyt1fdwmbHsRnL9RhVG5cIlxL86GLMKJfvfnTy8R9pJA0CKA9YBXK93Zq4V0GPzqBS9U+mBWGFJ2Qb7JYKxPlIAmEhIbSou4EPSnzfnPSE3pAoUpeUTDGvTTNML1jzbzNBeeY12rnq5VyDY87rqP3iDvEkqJkN+iMR59QYgcQNHZf4dzJtsP3G1AIEJt4fzCoT134RvBDrtr7N+G23v8EdWad07EjPjP21Slz/84dIL3aj1OlUG/HZU2/QL2y+TwBpJLP/kDsqDkxoKayFxwBjjfAq4BCfcUcKjDWZFvZlT0mXwIDAQAB");
//			item.setData(data);
//			updateData(item);  // TODO remove hardcodes, used to pass key
//		}
	}

	/**
	 * Set up IabHelper to get inventory of bought items and init data to be able launch purchase flow
	 */
	private void setupIabHelper(MembershipKeyItem.Data data) {
		mHelper = new IabHelper(getActivity(), data.getPublicKey());
		mHelper.enableDebugLogging(false);
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
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
		});
	}

	// Listener that's called when we finish querying the items and subscriptions we own
	private class GotInventoryListener implements  IabHelper.QueryInventoryFinishedListener {

		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
			if (isPaused || getActivity() == null) {
				return;
			}

			setWaitScreen(false);
			Log.d(TAG, "Query inventory finished.");
			if (result.isFailure()) {

				showSinglePopupDialog("Failed to query inventory: " + result);
				return;
			}

			if (!mHelper.subscriptionsSupported()) {
				showSinglePopupDialog("Subscriptions not supported on your device yet. Sorry!");
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
				isGoldMonthPayed = purchase != null && verifyDeveloperPayload(purchase);
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

			setWaitScreen(false); // TODO show another title

			// query our server for membership bought from non Google Play( Apple, Web)
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

			new RequestJsonTask<MembershipItem>(detailsListener).executeTask(loadItem); // TODO set proper item
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

			// update selected modes
			if (returnedObj.getData().getIs_premium() > 0) {
				AppData.setUserPremiumStatus(getActivity(), returnedObj.getData().getLevel());

				String sku = returnedObj.getData().getSku();

				// check if we bought an item in GP, but didn't update our server
				if (isGoldMonthPayed && !sku.equals("gold_monthly")) {

				}


				if (isGoldMonthPayed && !sku.equals("gold_monthly")) { // TODO set constants

//					isGoldMonthPayed = true;
				} else if (isGoldYearPayed && !sku.equals("gold_yearly")) {
//					isGoldYearPayed = true;
				} else if (isPlatinumMonthPayed && !sku.equals("platinum_monthly")) {
//					isPlatinumMonthPayed = true;
				} else if (isPlatinumYearPayed && !sku.equals("platinum_yearly")) {
//					isPlatinumYearPayed = true;
				} else if (isDiamondMonthPayed && !sku.equals("diamond_monthly")) {
//					isDiamondMonthPayed = true;
				} else if (isDiamondYearPayed && !sku.equals("diamond_yearly")) {
//					isDiamondYearPayed = true;
				}

				if (sku.equals("gold_monthly")) { // TODO set constants

//					isGoldMonthPayed = true;
				} else if (sku.equals("gold_yearly")) {
//					isGoldYearPayed = true;
				} else if (sku.equals("platinum_monthly")) {
//					isPlatinumMonthPayed = true;
				} else if (sku.equals("platinum_yearly")) {
//					isPlatinumYearPayed = true;
				} else if (sku.equals("diamond_monthly")) {
//					isDiamondMonthPayed = true;
				} else if (sku.equals("diamond_yearly")) {
//					isDiamondYearPayed = true;
				}
			}

			UpgradeDetailsFragment.this.updateData();
		}
	}

	private void updateMembershipOnServer() {

/*
		 String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
      String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
[27.05.2013 21:47:26] Chess.com Lackovic Ivan: you will pass the whole purchase data json string
[27.05.2013 21:47:28] Chess.com Lackovic Ivan: and the signature
[27.05.2013 21:47:34] roger: '{
   "orderId":"12999763169054705758.1371079406387615",
   "packageName":"com.example.app",
   "productId":"exampleSku",
   "purchaseTime":1345678900000,
   "purchaseState":0,
   "developerPayload":"bGoa+V7g/yqDXvKRqq+JTFn4uQZbPiQJo4pf9RzJ",
   "purchaseToken":"rojeslcdyyiapnqcynkjyyjh"
 }'
		 */

		LoadItem loadItem = new LoadItem();
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
//		loadItem.addRequestParams(RestHelper.P_PRODUCT_SKU, itemId);
//
//		new RequestJsonTask<PayloadItem>(new GetPayloadListener(itemId)).executeTask(loadItem);
	}

	private void sendPaymentRequest(String itemId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_MEMBERSHIP_PAYLOAD);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_PRODUCT_SKU, itemId);

		new RequestJsonTask<PayloadItem>(new GetPayloadListener(itemId)).executeTask(loadItem);
	}

	private class GetPayloadListener extends ChessUpdateListener<PayloadItem> {

		private String targetSku;

		private GetPayloadListener(String targetSku) {
			super(PayloadItem.class);
			this.targetSku = targetSku;
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

			sendPayment(targetSku);
		}
	}

	private void sendPayment(String itemId) {
		String payload = payloadData.getPayload();

		setWaitScreen(true);
		mHelper.launchPurchaseFlow(getActivity(), itemId, IabHelper.ITEM_TYPE_SUBS,
				RC_REQUEST, new PurchaseFinishedListener(), payload);
	}

	// Callback for when a purchase is finished
	private class PurchaseFinishedListener implements IabHelper.OnIabPurchaseFinishedListener {

		@Override
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			if (getActivity() == null) {
				logTest("onIabPurchaseFinished - >activity null");
				return;
			}
			Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
			if (result.isFailure()) {
				showSinglePopupDialog("Error purchasing: " + result);
				setWaitScreen(false);
				return;
			}
			if (!verifyDeveloperPayload(purchase)) {
				showSinglePopupDialog("Oops. Authenticity verification failed."
						+ " payload = " + purchase.getDeveloperPayload());
				logTest("oops - user =" + AppData.getUserName(getActivity())
						+ " order = " + purchase.getSku() + "payload = " + purchase.getDeveloperPayload());
				setWaitScreen(false);
				return;
			}

			Log.d(TAG, "Purchase successful.");

			if (purchase.getSku().equals(IabHelper.SKU_GOLD_MONTH)) {
				// bought the infinite gas subscription
				Log.d(TAG, IabHelper.SKU_GOLD_MONTH  + " subscription purchased.");
				showSinglePopupDialog("Thank you for subscribing to !" + IabHelper.SKU_GOLD_MONTH);
				isGoldMonthPayed = true;
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
	}

	/** Verifies the developer payload of a purchase. */
	boolean verifyDeveloperPayload(Purchase p) {
		String payload = p.getDeveloperPayload();
		if (payloadData == null)
			return true;
		return payloadData.getPayload().equals(payload);
	}

	private void setWaitScreen(boolean show) {
		if (show){
			showPopupProgressDialog(R.string.processing_);
		} else {
			dismissProgressDialog();
		}
	}
}
