package com.chess.clock.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.entities.AppTheme;
import com.chess.clock.views.ViewUtils;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder> {
    public AppTheme selectedTheme;

    public ThemesAdapter(AppTheme initialTheme) {
        setHasStableIds(true);
        this.selectedTheme = initialTheme;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        AppTheme theme = AppTheme.fromInt(position);
        holder.bind(theme, theme == selectedTheme);
        holder.themeCard.setOnClickListener(v -> {
            selectedTheme = theme;
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return AppTheme.values().length;
    }

    public static class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checkmarkImg;
        private final CardView themeCard;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            checkmarkImg = itemView.findViewById(R.id.checkmarkImg);
            themeCard = itemView.findViewById(R.id.themeCard);
        }

        public void bind(AppTheme appTheme, boolean selected) {
            int color = ContextCompat.getColor(itemView.getContext(), appTheme.primaryColorRes);
            themeCard.setCardBackgroundColor(color);
            ViewUtils.showView(checkmarkImg, selected);
        }
    }
}
