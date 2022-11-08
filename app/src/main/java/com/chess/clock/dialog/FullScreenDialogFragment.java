package com.chess.clock.dialog;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.chess.clock.R;

abstract class FullScreenDialogFragment extends DialogFragment {
    abstract int layoutRes();

    public int bgColorRes() {
        return R.color.gray_background;
    }

    public int theme() {
        return R.style.AppTheme_DialogFullScreen;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutRes(), container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireActivity(), theme());
        FrameLayout content = new FrameLayout(requireActivity());
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dialog.setContentView(content);
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(ContextCompat.getColor(requireContext(), bgColorRes()))
        );
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return dialog;
    }
}
