package com.tribe.app.presentation.view.adapter.delegate;

import android.app.Activity;
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
import com.tribe.app.presentation.internal.di.components.FriendshipComponent;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserGridAdapterDelegate implements AdapterDelegate<List<MarvelCharacter>> {

    @Inject PaletteGrid paletteGrid;
    private LayoutInflater layoutInflater;
    private OnFriendClickListener onFriendClickListener;

    public interface OnFriendClickListener {
        void onTextClickListener(MarvelCharacter marvelCharacter);
    }

    public UserGridAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<MarvelCharacter> items, int position) {
        return position != 0;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new UserGridViewHolder(layoutInflater.inflate(R.layout.item_user_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<MarvelCharacter> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserGridViewHolder vh = (UserGridViewHolder) holder;
        MarvelCharacter marvelCharacter = (MarvelCharacter) items.get(position);

        vh.txtName.setText(marvelCharacter.getName());
        vh.layoutContent.setBackgroundColor(paletteGrid.get(position - 1));

        vh.btnText.setOnClickListener(v -> onFriendClickListener.onTextClickListener(marvelCharacter));
    }

    public void setOnFriendClickListener(OnFriendClickListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.btnText) public ImageView btnText;
        @BindView(R.id.btnMore) public ImageView btnMore;
        @BindView(R.id.layoutContent) public ViewGroup layoutContent;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
