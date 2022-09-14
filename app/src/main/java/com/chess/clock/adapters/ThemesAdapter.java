package com.chess.clock.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.entities.AppTheme;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder> {

    public ThemesAdapter() {
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        holder.bind(AppTheme.fromInt(position));
    }

    @Override
    public int getItemCount() {
        return AppTheme.values().length;
    }

    public static class ThemeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView checkmarkIMg;
        private final CardView themeCard;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            checkmarkIMg = itemView.findViewById(R.id.checkmarkImg);
            themeCard = itemView.findViewById(R.id.themeCard);
        }

        public void bind(AppTheme appTheme) {
            themeCard.setCardBackgroundColor(appTheme.colorRes);
        }
    }
}
