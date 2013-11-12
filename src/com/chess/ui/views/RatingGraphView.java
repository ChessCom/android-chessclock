package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import com.chess.R;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboRadioButton;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.08.13
 * Time: 19:22
 */
public class RatingGraphView extends LinearLayout {

	private ChartView chartView;
	private float chartSidesAspectRatio;

	public RatingGraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setOrientation(VERTICAL);

		Resources resources = context.getResources();

		int paddingTop = resources.getDimensionPixelSize(R.dimen.graph_view_padding_top);
		int paddingSide = resources.getDimensionPixelSize(R.dimen.graph_view_padding_side);

		setPadding(0, paddingTop, 0, paddingTop);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.setMargins(paddingSide, 0, paddingSide, 0);

		{// ChartView
			//adjust height to match design
			int chartWidth = getResources().getInteger(R.integer.rating_graph_width);
			int chartHeight = getResources().getInteger(R.integer.rating_graph_height);

			chartSidesAspectRatio = (float) chartHeight / (float) chartWidth;

			int widthPixels = getResources().getDisplayMetrics().widthPixels;
			int height = (int) (widthPixels * chartSidesAspectRatio);

			LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);

			chartView = new ChartView(context);

			// actual params updates in onSizeChanged callback
			addView(chartView, chartParams);
		}

		{ // | Sept | Oct | Nov |
			RadioGroup.LayoutParams wideParams = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			wideParams.weight = 1;

			LinearLayout radioGroupLayout = new LinearLayout(context);
			radioGroupLayout.setOrientation(HORIZONTAL);
			int textColor = resources.getColor(R.color.stats_label_grey);

			{
				RoboButton textView = new RoboButton(context, null, R.attr.sideGraphBtn);
				textView.setDrawableStyle(R.style.Rect_Side_Left);
				textView.setFont(FontsHelper.DEFAULT_FONT);
				textView.setTextColor(textColor);
				textView.setText("Sep");
				textView.setGravity(Gravity.CENTER);

				radioGroupLayout.addView(textView, wideParams);
			}
			{
				RoboButton textView = new RoboButton(context, null, R.attr.sideGraphBtn);
				textView.setTextColor(textColor);
				textView.setDrawableStyle(R.style.Rect_Side_Middle);
				textView.setFont(FontsHelper.DEFAULT_FONT);
				textView.setText("Oct");
				textView.setGravity(Gravity.CENTER);

				radioGroupLayout.addView(textView, wideParams);
			}

			{
				RoboButton textView = new RoboButton(context, null, R.attr.sideGraphBtn);
				textView.setDrawableStyle(R.style.Rect_Side_Right);
				textView.setFont(FontsHelper.DEFAULT_FONT);
				textView.setTextColor(textColor);
				textView.setText("Nov");
				textView.setGravity(Gravity.CENTER);

				radioGroupLayout.addView(textView, wideParams);

				radioGroupLayout.setPadding(0, paddingTop, 0, paddingTop);
			}

			addView(radioGroupLayout, params);
		}

		{// add radioGroup
			RadioGroup.LayoutParams buttonParams = new RadioGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
			buttonParams.weight = 1;

			RadioGroup radioGroup = new RadioGroup(context);
			radioGroup.setOrientation(RadioGroup.HORIZONTAL);

			// | 30 Days  | 90 Days | 1 Year | All Time |

			{ // 30 Days button
				RoboRadioButton radioButton = new RoboRadioButton(context, null, R.attr.roundedRadioBtn);
				radioButton.setId(R.id.thirtyDaysBtn);
				radioButton.setTextAppearance(context, R.style.RoundRadioButton_Left);
				radioButton.setBackgroundResource(R.drawable.button_toggle_left_selector);
				radioButton.setText(R.string.thirty_days);
				radioButton.setFont(FontsHelper.BOLD_FONT);

				radioGroup.addView(radioButton, buttonParams);
			}

			{ // 90 Days button
				RoboRadioButton radioButton = new RoboRadioButton(context, null, R.attr.roundedRadioBtn);
				radioButton.setId(R.id.ninetyDaysBtn);
				radioButton.setTextAppearance(context, R.style.RoundRadioButton_Middle);
				radioButton.setBackgroundResource(R.drawable.button_toggle_middle_selector);
				radioButton.setText(R.string.ninety_days);
				radioButton.setFont(FontsHelper.BOLD_FONT);

				radioGroup.addView(radioButton, buttonParams);
			}

			{ // 1 Year button
				RoboRadioButton radioButton = new RoboRadioButton(context, null, R.attr.roundedRadioBtn);
				radioButton.setId(R.id.oneYearBtn);
				radioButton.setTextAppearance(context, R.style.RoundRadioButton_Middle);
				radioButton.setBackgroundResource(R.drawable.button_toggle_middle_selector);
				radioButton.setText(R.string.one_year);
				radioButton.setFont(FontsHelper.BOLD_FONT);

				radioGroup.addView(radioButton, buttonParams);
			}

			{ // All Time
				RoboRadioButton radioButton = new RoboRadioButton(context, null, R.attr.roundedRadioBtn);
				radioButton.setTextAppearance(context, R.style.RoundRadioButton_Right);
				radioButton.setId(R.id.allTimeBtn);
				radioButton.setBackgroundResource(R.drawable.button_toggle_right_selector);
				radioButton.setText(R.string.all_time);
				radioButton.setFont(FontsHelper.BOLD_FONT);
//				radioButton.setChecked(true);

				radioGroup.addView(radioButton, buttonParams);
			}

			addView(radioGroup, params);
		}
	}

	@Override
	protected void onSizeChanged(int width, int h, int oldw, int oldh) {
		super.onSizeChanged(width, h, oldw, oldh);

		chartView.getLayoutParams().height = (int) (width * chartSidesAspectRatio);
	}

	public void setGraphData(List<long[]> series, int width) {
		// TODO get timestamps
		chartView.setGraphData(series, width);
		invalidate();
	}
}
