package com.chess.ui.fragments.popup_fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.ImageCache;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.tasks.RequestJsonTask;
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
public class PopupBackgroundsFragment extends DialogFragment implements AdapterView.OnItemClickListener {

	private static final String THEME_ITEM = "theme_item";
	private static final String IMAGE_CACHE_DIR = "popup_backgrounds_previews";

	private PopupListSelectionFace listener;
	private BackgroundsUpdateListener backgroundsUpdateListener;
	private AppData appData;
	private ThemeItem.Data themeItem;
	private ListView listView;
	private List<SelectionItem> backgroundsList;
	private BackgroundsAdapter backgroundsAdapter;
	private boolean need2update = true;
	private View loadingView;
	private List<ThemeItem.Data> backgroundsThemeList;
	private SmartImageFetcher imageFetcher;

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

		if (getArguments() != null) {
			themeItem = getArguments().getParcelable(THEME_ITEM);
		} else {
			themeItem = savedInstanceState.getParcelable(THEME_ITEM);
		}

		appData = new AppData(getActivity());

		backgroundsList = new ArrayList<SelectionItem>();
		backgroundsUpdateListener = new BackgroundsUpdateListener();

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		imageFetcher = new SmartImageFetcher(getActivity());
		imageFetcher.setLoadingImage(R.drawable.img_profile_picture_stub);
		imageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
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
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					imageFetcher.setPauseWork(true);
				} else {
					imageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_THEMES);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());

			new RequestJsonTask<ThemeItem>(backgroundsUpdateListener).executeTask(loadItem);
		} else {
			listView.setAdapter(backgroundsAdapter);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelable(THEME_ITEM, themeItem);
	}

	public ThemeItem.Data getItemByCode(int code) {
		return backgroundsThemeList.get(code);
	}

	private class BackgroundsUpdateListener extends ActionBarUpdateListener<ThemeItem> {

		private BackgroundsUpdateListener() {
			super((CoreActivityActionBar) getActivity(), PopupBackgroundsFragment.this, ThemeItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
		}

		@Override
		public void updateData(ThemeItem returnedObj) {
			backgroundsThemeList = returnedObj.getData();

			for (ThemeItem.Data theme : backgroundsThemeList) {
				SelectionItem selectionItem = new SelectionItem(null, theme.getBackgroundPreviewUrl());
				selectionItem.setCode(theme.getThemeName());
				backgroundsList.add(selectionItem);
			}

			for (SelectionItem selectionItem : backgroundsList) {
				if (selectionItem.getCode().equals(themeItem.getThemeName())) {
					selectionItem.setChecked(true);
					break;
				}
			}
			backgroundsAdapter = new BackgroundsAdapter(getActivity(), backgroundsList, imageFetcher);
			listView.setAdapter(backgroundsAdapter);

			need2update = false;
		}
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
		private final float aspectRatio;
		private final RelativeLayout.LayoutParams imageParams;
		private final RelativeLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;

		public BackgroundsAdapter(Context context, List<SelectionItem> menuItems, SmartImageFetcher imageFetcher) {
			super(context, menuItems, imageFetcher);

			previewWidth = PopupBackgroundsFragment.this.getView().getWidth();
			aspectRatio = 1f / 2.9f;

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);
			int imageHeight = (int) (previewWidth * aspectRatio);
			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
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

			imageFetcher.loadImage(new SmartImageFetcher.Data(item.getText(), previewWidth), holder.image.getImageView());

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
