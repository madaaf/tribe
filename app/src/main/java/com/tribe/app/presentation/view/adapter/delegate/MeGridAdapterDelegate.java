package com.tribe.app.presentation.view.adapter.delegate;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class MeGridAdapterDelegate implements AdapterDelegate<List<MarvelCharacter>> {

    protected LayoutInflater layoutInflater;
    @Inject PaletteGrid paletteGrid;

    public MeGridAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<MarvelCharacter> items, int position) {
        return (position == 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new MeGridViewHolder(layoutInflater.inflate(R.layout.item_me_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<MarvelCharacter> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        MeGridViewHolder vh = (MeGridViewHolder) holder;
        MarvelCharacter marvelCharacter = (MarvelCharacter) items.get(position);
        vh.layoutContent.setBackgroundColor(Color.parseColor("#094D92"));
    }

    static class MeGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContent) public ViewGroup layoutContent;

        public MeGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
