package com.chess.ui.fragments.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.GetAndSavePieces;
import com.chess.backend.GetAndSaveTheme;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.themes.PieceSingleItem;
import com.chess.backend.entity.api.themes.PiecesItem;
import com.chess.backend.image_load.EnhancedImageDownloader;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.interfaces.FileReadyListener;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.SelectionItem;
import com.chess.statics.AppConstants;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.10.13
 * Time: 16:15
 */
public class SettingsThemePiecesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	private PiecesItemUpdateListener piecesItemUpdateListener;
	private GetAndSavePieces.ServiceBinder serviceBinder;
	private LoadServiceConnectionListener loadServiceConnectionListener;
	private ProgressUpdateListener progressUpdateListener;

	private View headerView;
	private TextView progressTitleTxt;
	private ProgressBar themeLoadProgressBar;
	private ListView listView;

	private ThemePiecesAdapter themePiecesAdapter;
	private DefaultPiecesAdapter defaultPiecesAdapter;

	private List<SelectionItem> defaultPiecesSelectionList;
	private List<SelectionItem> themePiecesSelectionList;
	private List<PieceSingleItem.Data> themePiecesItemsList;

	private boolean isAuthenticatedUser;
	private boolean loadThemedPieces;
	private SelectionItem selectedThemePieceItem;
	private String themePiecesName;
	private boolean needToLoadThemeAfterConnected;
	private boolean serviceBounded;
	private boolean isPiecesLoading;
	private int selectedPiecesId;
	private FragmentParentFace parentFace;

	public SettingsThemePiecesFragment() {}

	public SettingsThemePiecesFragment(FragmentParentFace parentFace) {
		this.parentFace = parentFace;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_header_list_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.select_style);

		headerView = view.findViewById(R.id.headerView);
		progressTitleTxt = (TextView) headerView.findViewById(R.id.progressTitleTxt);
		themeLoadProgressBar = (ProgressBar) headerView.findViewById(R.id.themeLoadProgressBar);
		headerView.setVisibility(View.GONE);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setOnItemClickListener(this);
		themePiecesAdapter = new ThemePiecesAdapter(getContext(), null);
		defaultPiecesAdapter = new DefaultPiecesAdapter(getActivity(), defaultPiecesSelectionList);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Don't load custom pieces if we are not logged in
		if (isAuthenticatedUser && isNetworkAvailable()) {
			loadThemedPieces = true;
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.THEME_PIECES));

			if (cursor != null && cursor.moveToFirst() && getAppData().isThemePiecesLoaded()) {
				do {
					themePiecesItemsList.add(DbDataManager.getThemePieceItemFromCursor(cursor));
				} while (cursor.moveToNext());

				updateUiData();
			} else {
				getPieces();
			}
		} else {
			listView.setAdapter(defaultPiecesAdapter);
		}
	}

	private void getPieces() {
		// load pieces line previews
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_PIECES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<PiecesItem>(piecesItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (loadThemedPieces) {
			if (isPiecesLoading) {
				return;
			}

			// don't allow to select while it's loading
			isPiecesLoading = true;

			selectedThemePieceItem = (SelectionItem) parent.getItemAtPosition(position);
			for (SelectionItem selectionItem : themePiecesSelectionList) {
				if (selectedThemePieceItem.getCode().equals(selectionItem.getCode())) {
					selectionItem.setChecked(true);
				} else {
					selectionItem.setChecked(false);
				}
			}

			themePiecesAdapter.notifyDataSetChanged();

			selectedPiecesId = 1;
			for (PieceSingleItem.Data data : themePiecesItemsList) {
				if (data.getName().equals(selectedThemePieceItem.getCode())) {
					selectedPiecesId = data.getThemePieceId();
					break;
				}
			}

			if (serviceBounded) {
				serviceBinder.getService().loadPieces(selectedPiecesId, screenWidth);
			} else {
				needToLoadThemeAfterConnected = true;
				getActivity().bindService(new Intent(getActivity(), GetAndSavePieces.class), loadServiceConnectionListener,
						Activity.BIND_AUTO_CREATE);
			}

		} else {
			SelectionItem defaultPieceItem = (SelectionItem) parent.getItemAtPosition(position);
			for (SelectionItem selectionItem : defaultPiecesSelectionList) {
				if (defaultPieceItem.getText().equals(selectionItem.getText())) {
					selectionItem.setChecked(true);
					// save pieces theme name to appData
					getAppData().setUseThemePieces(false);
					getAppData().setThemePiecesName(selectionItem.getText());
				} else {
					selectionItem.setChecked(false);
				}
			}

			if (!isTablet) {// go back
				getActivityFace().showPreviousFragment();
			} else {
				if (parentFace != null) {
					parentFace.showPreviousFragment();
				}
			}
		}
	}

	private class PiecesItemUpdateListener extends ChessLoadUpdateListener<PiecesItem> {

		private PiecesItemUpdateListener() {
			super(PiecesItem.class);
		}

		@Override
		public void updateData(PiecesItem returnedObj) {
			super.updateData(returnedObj);

			themePiecesItemsList = returnedObj.getData();
			for (PieceSingleItem.Data currentItem : themePiecesItemsList) {
				DbDataManager.saveThemePieceItemToDb(getContentResolver(), currentItem);
			}

			getAppData().setThemePiecesLoaded(true);
			updateUiData();
		}
	}

	private void updateUiData() {
		for (PieceSingleItem.Data pieceItem : themePiecesItemsList) {
			SelectionItem selectionItem = new SelectionItem(null, pieceItem.getPreviewUrl());
			selectionItem.setCode(pieceItem.getName());
			themePiecesSelectionList.add(selectionItem);
		}

		for (SelectionItem selectionItem : themePiecesSelectionList) {
			if (selectionItem.getCode().equals(themePiecesName)) {
				selectionItem.setChecked(true);
				break;
			}
		}
		themePiecesAdapter.setItemsList(themePiecesSelectionList);
		themePiecesAdapter.notifyDataSetChanged();

		listView.setAdapter(themePiecesAdapter);
	}

	private class LoadServiceConnectionListener implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			serviceBounded = true;

			serviceBinder = (GetAndSavePieces.ServiceBinder) iBinder;
			serviceBinder.getService().setProgressUpdateListener(progressUpdateListener);

			if (serviceBinder.getService().isInstallingPieces()) {
				isPiecesLoading = true;
			}
			if (needToLoadThemeAfterConnected) {
				serviceBinder.getService().loadPieces(selectedPiecesId, screenWidth);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBounded = false;
			isPiecesLoading = false;
		}
	}

	private void init() {
		isAuthenticatedUser = !TextUtils.isEmpty(getUserToken());

		screenWidth = getResources().getDisplayMetrics().widthPixels;

		themePiecesName = getAppData().getThemePiecesName();

		piecesItemUpdateListener = new PiecesItemUpdateListener();
		loadServiceConnectionListener = new LoadServiceConnectionListener();
		progressUpdateListener = new ProgressUpdateListener();

		Resources resources = getResources();

		themePiecesSelectionList = new ArrayList<SelectionItem>();
		themePiecesItemsList = new ArrayList<PieceSingleItem.Data>();

		// Pieces bitmaps list init
		defaultPiecesSelectionList = new ArrayList<SelectionItem>();
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_classic), getString(R.string.pieces_classic)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_alpha), getString(R.string.pieces_alpha)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_modern), getString(R.string.pieces_modern)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_book), getString(R.string.pieces_book)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_club), getString(R.string.pieces_club)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_game), getString(R.string.pieces_game)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_cases), getString(R.string.pieces_cases)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_condal), getString(R.string.pieces_condal)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_vintage), getString(R.string.pieces_vintage)));
		defaultPiecesSelectionList.add(new SelectionItem(resources.getDrawable(R.drawable.pieces_maya), getString(R.string.pieces_maya)));

		if (!isAuthenticatedUser) {
			if (TextUtils.isEmpty(themePiecesName)) {
				themePiecesName = getString(R.string.pieces_game);
			}

			for (SelectionItem selectionItem : defaultPiecesSelectionList) {
				if (selectionItem.getText().equals(themePiecesName)) {
					selectionItem.setChecked(true);
					break;
				}
			}
		} else {
			if (TextUtils.isEmpty(themePiecesName)) {
				themePiecesName = AppConstants.DEFAULT_THEME_NAME; // use theme name instead of pieces
			}
		}
	}

	private class ProgressUpdateListener implements FileReadyListener {

		@Override
		public void changeTitle(final String title) {
			if (getActivity() == null) {
				return;
			}
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (isPiecesLoading) {
						headerView.setVisibility(View.VISIBLE);
						progressTitleTxt.setText(title);
						progressTitleTxt.setVisibility(View.VISIBLE);
					} else {
						headerView.setVisibility(View.GONE);
						progressTitleTxt.setVisibility(View.VISIBLE);
					}
				}
			});

		}

		@Override
		public void setProgress(final int progress) {
			if (getActivity() == null) {
				return;
			}

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (progress == GetAndSaveTheme.DONE) {
						isPiecesLoading = false;
						headerView.setVisibility(View.GONE);

						if (serviceBounded) {
							getActivity().unbindService(loadServiceConnectionListener);
						}
						serviceBounded = false;
						if (!isTablet) {// go back
							getActivityFace().showPreviousFragment();
						} else {
							if (parentFace != null) {
								parentFace.showPreviousFragment();
							}
						}
					} else {
						if (isPiecesLoading) {
							headerView.setVisibility(View.VISIBLE);
							if (progress != GetAndSaveTheme.INDETERMINATE) {
								themeLoadProgressBar.setProgress(progress);
								themeLoadProgressBar.setIndeterminate(false);
							} else {
								themeLoadProgressBar.setIndeterminate(true);
							}

							themeLoadProgressBar.setVisibility(View.VISIBLE);
						} else {
							headerView.setVisibility(View.GONE);
						}
					}
				}
			});

		}
	}

	private class ThemePiecesAdapter extends ItemsAdapter<SelectionItem> {

		private final int previewWidth;
		private final Bitmap placeHolderBitmap;
		//			private final float aspectRatio;
		private final RelativeLayout.LayoutParams imageParams;
		private final LinearLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;
		private final EnhancedImageDownloader imageLoader;

		public ThemePiecesAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);

			imageLoader = new EnhancedImageDownloader(context);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			Drawable piecesDrawableExample = resources.getDrawable(R.drawable.pieces_alpha);
			previewWidth = piecesDrawableExample.getIntrinsicWidth();
			int imageHeight = piecesDrawableExample.getIntrinsicHeight();

			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new LinearLayout.LayoutParams(previewWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			View view = inflater.inflate(R.layout.selection_progress_image_item, parent, false);
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

	private class DefaultPiecesAdapter extends ItemsAdapter<SelectionItem> {

		private final int previewWidth;
		private final Bitmap placeHolderBitmap;
		//			private final float aspectRatio;
		private final RelativeLayout.LayoutParams imageParams;
		private final LinearLayout.LayoutParams linearLayoutParams;
		private final RelativeLayout.LayoutParams progressParams;

		public DefaultPiecesAdapter(Context context, List<SelectionItem> menuItems) {
			super(context, menuItems);

			int backIMgColor = getResources().getColor(R.color.upgrade_toggle_button_p);
			placeHolderBitmap = Bitmap.createBitmap(new int[]{backIMgColor}, 1, 1, Bitmap.Config.ARGB_8888);

			Drawable piecesDrawableExample = resources.getDrawable(R.drawable.pieces_alpha);
			previewWidth = piecesDrawableExample.getIntrinsicWidth();
			int imageHeight = piecesDrawableExample.getIntrinsicHeight();


			imageParams = new RelativeLayout.LayoutParams(previewWidth, imageHeight);
			linearLayoutParams = new LinearLayout.LayoutParams(previewWidth, imageHeight);

			progressParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			progressParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		}

		@Override
		protected View createView(ViewGroup parent) { // View to display in layout
			View view = inflater.inflate(R.layout.selection_progress_image_item, parent, false);
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

			holder.image.setImageDrawable(item.getImage());

			holder.text.setText(item.getText());
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
