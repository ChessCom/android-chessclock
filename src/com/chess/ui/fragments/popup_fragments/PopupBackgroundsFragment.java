package com.chess.ui.fragments.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.BackgroundSingleItem;
import com.chess.backend.entity.api.themes.BackgroundsItem;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.SelectionItem;
import com.chess.statics.AppData;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.ActionBarBackgroundDrawable;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 15:00
 */
public class PopupBackgroundsFragment extends DialogFragment implements AdapterView.OnItemClickListener, ViewTreeObserver.OnGlobalLayoutListener {

	private static final String THEME_ITEM = "theme_item";

	private PopupListSelectionFace listener;
	private BackgroundsUpdateListener backgroundsUpdateListener;
	private AppData appData;
	private ThemeItem.Data themeItem;
	private ListView listView;
	private List<SelectionItem> backgroundsList;
	private BackgroundsAdapter backgroundsAdapter;
	private boolean need2update = true;
	private View loadingView;
	private List<BackgroundSingleItem.Data> backgroundsThemeList;
	private boolean isTablet;
	private boolean adapterSet;

	public PopupBackgroundsFragment() {
	}

	public static PopupBackgroundsFragment createInstance(PopupListSelectionFace listener, ThemeItem.Data themeItem) {
		PopupBackgroundsFragment fragment = new PopupBackgroundsFragment();
		fragment.listener = listener;
		Bundle bundle = new Bundle();
		bundle.putParcelable(THEME_ITEM, themeItem);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);

		isTablet = AppUtils.isTablet(getActivity());

		if (getArguments() != null) {
			themeItem = getArguments().getParcelable(THEME_ITEM);
		} else {
			themeItem = savedInstanceState.getParcelable(THEME_ITEM);
		}

		appData = new AppData(getActivity());

		backgroundsList = new ArrayList<SelectionItem>();
		backgroundsUpdateListener = new BackgroundsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
		return inflater.inflate(R.layout.new_popup_list_selection_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View popupTitleLay = view.findViewById(R.id.popupTitleLay);
		if (AppUtils.JELLYBEAN_PLUS_API) {
			popupTitleLay.setBackground(new ActionBarBackgroundDrawable(getActivity()));
		} else {
			popupTitleLay.setBackgroundDrawable(new ActionBarBackgroundDrawable(getActivity()));
		}

		((TextView) view.findViewById(R.id.popupTitleTxt)).setText(R.string.select_background);

		loadingView = view.findViewById(R.id.loadingView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);

		getView().getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!need2update) {
			listView.setAdapter(backgroundsAdapter);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(THEME_ITEM, themeItem);
	}

	public BackgroundSingleItem.Data getItemByCode(int code) {
		return backgroundsThemeList.get(code);
	}

	@Override
	public void onGlobalLayout() {
		if (getView() != null & getView().getWidth() != 0 && !adapterSet) {
			adapterSet = true;
			Cursor cursor = DbDataManager.query(getActivity().getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEME_BACKGROUNDS));

			if (cursor != null && cursor.moveToFirst() && appData.isThemeBackgroundsLoaded()) {
				backgroundsThemeList = new ArrayList<BackgroundSingleItem.Data>();
				do {
					backgroundsThemeList.add(DbDataManager.getThemeBackgroundItemFromCursor(cursor));
				} while (cursor.moveToNext());
				cursor.close();

				updateUiData();
			} else {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_BACKGROUNDS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());

				new RequestJsonTask<BackgroundsItem>(backgroundsUpdateListener).executeTask(loadItem);
			}
		}
	}

	private class BackgroundsUpdateListener extends ActionBarUpdateListener<BackgroundsItem> {

		private BackgroundsUpdateListener() {
			super((CoreActivityActionBar) getActivity(), PopupBackgroundsFragment.this, BackgroundsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
		}

		@Override
		public void updateData(BackgroundsItem returnedObj) {
			backgroundsThemeList = returnedObj.getData();

			for (BackgroundSingleItem.Data data : backgroundsThemeList) {
				DbDataManager.saveThemeBackgroundItemToDb(getActivity().getContentResolver(), data);
			}

			appData.setThemeBackgroundsLoaded(true);
			updateUiData();
		}
	}

	private void updateUiData() {
		for (BackgroundSingleItem.Data theme : backgroundsThemeList) {
			SelectionItem selectionItem = new SelectionItem(null, theme.getBackgroundPreviewUrl());
			selectionItem.setCode(theme.getName());
			backgroundsList.add(selectionItem);
		}

		for (SelectionItem selectionItem : backgroundsList) {
			if (selectionItem.getCode().equals(themeItem.getThemeName())) {
				selectionItem.setChecked(true);
				break;
			}
		}
		backgroundsAdapter = new BackgroundsAdapter(getActivity(), backgroundsList);
		listView.setAdapter(backgroundsAdapter);

		need2update = false;
	}


	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		if (listener != null) {
			listener.onDialogCanceled();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (listener == null) { // app has been killed during the background, but we restored w/o listener
			dismiss();
		} else {
			listener.onValueSelected(position);
		}
	}

	private class BackgroundsAdapter extends ItemsAdapter<SelectionItem> {

		private final int previewWidth;
		private final Bitmap placeHolderBitmap;
		private final RelativeLayout.LayoutParams imageParams;
		private final FrameLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;
		private final EnhancedImageDownloader imageLoader;

		public BackgroundsAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);

			imageLoader = new EnhancedImageDownloader(context);

			previewWidth = PopupBackgroundsFragment.this.getView().getWidth();
			int imageHeight;
			if (!isTablet) {
				imageHeight = (int) (previewWidth / 2.9f);
			} else {
				imageHeight = (int) (previewWidth / 4.0f); // TODO move to resources
			}

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new FrameLayout.LayoutParams(previewWidth, imageHeight);
			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			View view = inflater.inflate(R.layout.new_background_preview_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.image = (ProgressImageView) view.findViewById(R.id.image);

			holder.image.setLayoutParams(linearLayoutParams);

			// Change Placeholder
			holder.image.placeholder = placeHolderBitmap;

			// Change Image params
			holder.image.getImageView().setLayoutParams(imageParams);
			holder.image.getImageView().setScaleType(ImageView.ScaleType.FIT_XY);

			// Change ProgressBar params
			holder.image.getProgressBar().setLayoutParams(progressParams);

			holder.text = (CheckedTextView) view.findViewById(R.id.text);

			view.setTag(holder);

			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View view) {
			ViewHolder holder = (ViewHolder) view.getTag();

			imageLoader.download(item.getText(), holder.image, previewWidth, previewWidth);
			holder.text.setText(item.getCode());
			holder.text.setChecked(item.isChecked());
		}

		private class ViewHolder {
			public CheckedTextView text;
			public ProgressImageView image;
		}

		public Context getContext() {
			return context;
		}
	}

}
