package com.chess.clock.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.chess.clock.R;

public class ViewUtils {
    public static void showView(View v, Boolean show) {
        if (show) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.GONE);
        }
    }

    public static void isInvisible(View v, boolean invisible) {
        if (invisible) {
            v.setVisibility(View.INVISIBLE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    /**
     * call before dialog `show()`
     */
    public static void setLargePopupMessageTextSize(Dialog dialog, Resources resources) {
        dialog.setOnShowListener(dialogInterface -> {
            TextView messageTv = dialog.findViewById(android.R.id.message);
            if (messageTv != null) {
                messageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.text_size_dialog_title));
            }
        });
    }

    public static Drawable getSelectableItemBgDrawable(Context context) {
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{R.attr.selectableItemBackground});
        int attributeResourceId = a.getResourceId(0, 0);
        return ContextCompat.getDrawable(context, attributeResourceId);
    }
}
