package com.chess.clock.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.chess.clock.R;

public class ViewUtils {
    public static void applyWindowInsets(Window window) {
        applyWindowInsets(window, false);
    }

    public static void applyWindowInsets(Window window, boolean reserveNavigationSpace) {
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setSoftInputMode((window.getAttributes().softInputMode
                & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST)
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        View content = window.findViewById(android.R.id.content);
        int left = content.getPaddingLeft();
        int top = content.getPaddingTop();
        int right = content.getPaddingRight();
        int bottom = content.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            int bottomInset = insets.bottom;
            if (reserveNavigationSpace) {
                bottomInset = Math.max(bottomInset, Math.max(
                        windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars()).bottom,
                        windowInsets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.mandatorySystemGestures()).bottom));
            }
            view.setPadding(left + insets.left, top + insets.top,
                    right + insets.right, bottom + bottomInset);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);
    }

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
        TypedArray a = context.getTheme().obtainStyledAttributes(R.style.AppTheme, new int[]{android.R.attr.selectableItemBackground});
        int attributeResourceId = a.getResourceId(0, 0);
        return ContextCompat.getDrawable(context, attributeResourceId);
    }
}
