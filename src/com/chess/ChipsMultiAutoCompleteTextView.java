package com.chess;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.MultiAutoCompleteTextView;
import com.chess.ui.views.drawables.IconDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.08.13
 * Time: 14:53
 */
public class ChipsMultiAutoCompleteTextView extends MultiAutoCompleteTextView implements OnItemClickListener {

	public static final String SPACE = ",";

	public ChipsMultiAutoCompleteTextView(Context context) {
		super(context);
		init(context);
	}

	public ChipsMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void init(Context context) {
		setOnItemClickListener(this);
		addTextChangedListener(textWatcher);
	}

	private TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (count >= 1) {
				if (s.charAt(start) == ',') {
					setChips(); // generate chips
				}
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	public void setChips() {
		Editable text = getText();
		if (text.toString().contains(SPACE)) {// check comma in string
			Resources resources = getResources();
			float density = resources.getDisplayMetrics().density;
			int textColor = resources.getColor(R.color.new_author_dark_grey);

			SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text);
			// split string with comma
			String chips[] = text.toString().trim().split(SPACE);
			int x = 0;
			// loop will generate ImageSpan for every country name separated by comma
			for (String chip : chips) {
				RoboTextView textView = new RoboTextView(getContext());
				ButtonDrawableBuilder.setBackgroundToView(textView, R.style.Button_Blue_Chip);

				textView.setTextSize(13);
				textView.setTextColor(textColor);
				textView.setText(chip);
				textView.setCompoundDrawablePadding((int) (4 * density));
				int sidePadding = (int) (6 * density);
				int topPadding = (int) (3 * density);
				textView.setPadding(sidePadding, topPadding, sidePadding, topPadding);

				IconDrawable iconDrawable = new IconDrawable(getContext(), R.string.ic_close, R.color.new_normal_grey_3,
						R.dimen.chip_close_icon_size);
				textView.setCompoundDrawablesWithIntrinsicBounds(null, null, iconDrawable, null);

				// capture bitmap of generated textView
				int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				textView.measure(spec, spec);
				textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
				Bitmap bitmap = Bitmap.createBitmap(textView.getWidth(), textView.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(bitmap);
				canvas.translate(-textView.getScrollX(), -textView.getScrollY());
				textView.draw(canvas);
				textView.setDrawingCacheEnabled(true);
				Bitmap cacheBmp = textView.getDrawingCache();
				Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
				textView.destroyDrawingCache();

				// create bitmap drawable for imageSpan
				BitmapDrawable bmpDrawable = new BitmapDrawable(resources, viewBmp);
				bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());

				// create and set imageSpan
				stringBuilder.setSpan(new ImageSpan(bmpDrawable), x, x + chip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				x = x + chip.length() + 1;
			}
			// set chips span 
			setText(stringBuilder);
			// move cursor to last 
			setSelection(text.length());
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		setChips(); // call generate chips when user select any item from auto complete
	}

}
