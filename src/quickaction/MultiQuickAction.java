package quickaction;

import android.content.Context;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.SelectionItem;
import com.chess.widgets.RoboButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.12.13
 * Time: 11:23
 */
public class MultiQuickAction extends PopupWindows implements PopupWindow.OnDismissListener {

	private final int shadowOffset;
	private View mRootView;
	private LayoutInflater mInflater;
	private ViewGroup itemsContainerView;
	private ScrollView scrollView;
	private OnActionItemClickListener mItemClickListener;
	private PopupWindow.OnDismissListener mDismissListener;

	private List<MultiActionItem> actionItems = new ArrayList<MultiActionItem>();

	private boolean mDidAction;

	private int mChildPos;
	private int mInsertPos;
	private int mAnimStyle;
	private int mOrientation;
	private int rootWidth = 0;

	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	private float density;
	private float buttonTextSize;
	private boolean useOnSide;

	/**
	 * Constructor for default vertical layout
	 *
	 * @param context Context
	 */
	public MultiQuickAction(Context context) {
		this(context, VERTICAL);
	}

	/**
	 * Constructor allowing orientation override
	 *
	 * @param context     Context
	 * @param orientation Layout orientation, can be vartical or horizontal
	 */
	public MultiQuickAction(Context context, int orientation) {
		super(context);

		density = context.getResources().getDisplayMetrics().density;

		mOrientation = orientation;
		buttonTextSize = 14;

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (mOrientation == HORIZONTAL) {
			setRootViewId(R.layout.multi_quick_popup_horizontal);
		} else {
			setRootViewId(R.layout.multi_quick_popup_vertical);
		}

		mAnimStyle = ANIM_REFLECT;
		mChildPos = 0;

		shadowOffset = context.getResources().getDimensionPixelSize(R.dimen.overlay_shadow_offset);
	}

	/**
	 * Get action item at an index
	 *
	 * @param index Index of item (position from callback)
	 * @return Action Item at the position
	 */
	public MultiActionItem getActionItem(int index) {
		return actionItems.get(index);
	}

	/**
	 * Set root view.
	 *
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		mRootView = (ViewGroup) mInflater.inflate(id, null);
		itemsContainerView = (ViewGroup) mRootView.findViewById(R.id.itemsContainerView);

		scrollView = (ScrollView) mRootView.findViewById(R.id.scrollView);
//		ButtonDrawableBuilder.setBackgroundToView(scrollView, R.style.ListItem_Header_2_Light);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mRootView.setLayoutParams(params);

		setContentView(mRootView);
	}

	/**
	 * Set animation style
	 *
	 * @param mAnimStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int mAnimStyle) {
		this.mAnimStyle = mAnimStyle;
	}

	/**
	 * Set listener for action item clicked.
	 *
	 * @param listener Listener
	 */
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mItemClickListener = listener;
	}

	/**
	 * Add actionItem item
	 *
	 * @param actionItem {@link ActionItem}
	 */
	public void addActionItem(MultiActionItem actionItem) {
		actionItems.add(actionItem);


		View container = mInflater.inflate(R.layout.multi_quick_action_item, null);

		TextView iconTxt = (TextView) container.findViewById(R.id.iconTxt);
		iconTxt.setText(actionItem.getIconId());
		if (useOnSide) {
			// hardcode bcz of icon
			if (actionItem.getIconId() == R.string.ic_live_bullet) {
				int padding = (int) (9 * density);
				iconTxt.setPadding(padding, padding, padding, padding);
			}
		} else {
			if (actionItem.getIconId() == R.string.ic_live_bullet) {
				int padding = (int) (9 * density);
				int sidePadding = (int) (4.5f * density);
				iconTxt.setPadding(sidePadding, padding, padding, padding);
			} else {
				int padding = (int) (10 * density);
				int sidePadding = (int) (5 * density);
				iconTxt.setPadding(sidePadding, padding, padding, padding);
			}
		}

		LinearLayout optionsView = (LinearLayout) container.findViewById(R.id.optionsView);
		final int pos = mChildPos;

		for (SelectionItem item : actionItem.getItems()) {
			RoboButton button = new RoboButton(context, null, R.attr.glassyButton);
			button.setDrawableStyle(R.style.Button_Glassy);
			button.setTextColor(context.getResources().getColor(R.color.stats_label_grey));
			button.setTextSize(buttonTextSize);
			button.setText(item.getText());
//			if (!useOnSide) {
//				button.setMinimumWidth((int) (62 * density));
//			}
			final int actionId = item.getId();

			button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mItemClickListener != null) {
						mItemClickListener.onItemClick(MultiQuickAction.this, pos, actionId);
					}

					mDidAction = true;

					dismiss();
				}
			});

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			int margin = (int) (1 * density);
			params.setMargins(margin, 0, margin, 0);
			optionsView.addView(button, params);
		}

		container.setFocusable(true);
		container.setClickable(true);

		itemsContainerView.addView(container, mInsertPos);

		mChildPos++;
		mInsertPos++;
	}

	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 */
	public void showOnSide(View anchor) {
		show(anchor, true);
	}

	public void show(View anchor) {
		show(anchor, false);
	}

	public void show(View anchor, boolean showOnSide) {
		preShow();

		int xPos, yPos;

		mDidAction = false;

		int[] location = new int[2];

		anchor.getLocationOnScreen(location);

		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1]
				+ anchor.getHeight());

		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		mRootView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		int rootHeight = mRootView.getMeasuredHeight();

		if (rootWidth == 0) {
			rootWidth = mRootView.getMeasuredWidth();
		}

		int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left /*- (rootWidth-anchor.getWidth())*/;
			xPos = (xPos < 0) ? 0 : xPos;

		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth / 2);
//				xPos = anchorRect.left;
			} else {
				xPos = anchorRect.left;
			}

		}

		int dyTop = anchorRect.top;
		int dyBottom = screenHeight - anchorRect.bottom;

//		boolean onTop = (dyTop > dyBottom);
		boolean onTop = false;

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos = 15;
				ViewGroup.LayoutParams params = scrollView.getLayoutParams();
				params.height = dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;

			if (rootHeight > dyBottom) {
				ViewGroup.LayoutParams params = scrollView.getLayoutParams();
				params.height = dyBottom;
			}
		}

		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

		if (showOnSide) {
			int offsetY = (int) (15 * density);
			int offsetX = (int) (25 * density);
			int centerY = anchorRect.bottom - anchorRect.top - offsetY;
			int leftSide = anchorRect.right - offsetX;

			mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, leftSide, centerY);
		} else {
			yPos -= shadowOffset * 2;
			mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		}
	}

	public boolean isUseOnSide() {
		return useOnSide;
	}

	public void setUseOnSide(boolean useOnSide) {
		this.useOnSide = useOnSide;
	}

	/**
	 * Set animation style
	 *
	 * @param screenWidth screen width
	 * @param requestedX  distance from left edge
	 * @param onTop       flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 *                    and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
//		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;

		switch (mAnimStyle) {
			case ANIM_GROW_FROM_LEFT:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
				break;

			case ANIM_GROW_FROM_RIGHT:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
				break;

			case ANIM_GROW_FROM_CENTER:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
				break;

			case ANIM_REFLECT:
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
				break;

			case ANIM_AUTO:
//			if (arrowPos <= screenWidth/4) {
//				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
//			} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
//				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
//			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
//			}

				break;
		}
	}

	/**
	 * Show arrow
	 *
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
//        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
//        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;
//
//        final int arrowWidth = mArrowUp.getMeasuredWidth();
//
//        showArrow.setVisibility(View.VISIBLE);
//
//        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
//
//        param.leftMargin = requestedX - arrowWidth / 2;
//
//        hideArrow.setVisibility(View.INVISIBLE);
	}

	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	@Override
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		super.setOnDismissListener(this);

		mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}

	/**
	 * Listener for item click
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(MultiQuickAction source, int pos, int actionId);
	}

//	/**
//	 * Listener for window dismiss
//	 */
//	public interface OnDismissListener {
//		public abstract void onDismiss();
//	}
}