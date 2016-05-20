package com.tribe.app.presentation.view.adapter.delegate;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserGridAdapterDelegate implements AdapterDelegate<List<MarvelCharacter>> {

    private LayoutInflater layoutInflater;

    public UserGridAdapterDelegate(Context context) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public boolean isForViewType(@NonNull List<MarvelCharacter> items, int position) {
        return true;
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
    }

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
