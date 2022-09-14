package com.chess.clock.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.chess.clock.R;

public class StyledButton extends CardView {

    public StyledButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        float radius = getResources().getDimension(R.dimen.default_radius);
        int elevation = getResources().getDimensionPixelSize(R.dimen.default_elevation);
        setRadius(radius);
        setCardElevation(elevation);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_styled_button, this, true);
        TextView text = view.findViewById(R.id.text);

        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.StyledButton, 0, 0);
            String buttonText = t.getString(R.styleable.StyledButton_android_text);
            t.recycle();
            text.setText(buttonText);
        }
    }
}
