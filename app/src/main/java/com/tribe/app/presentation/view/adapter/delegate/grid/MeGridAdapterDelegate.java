package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tiago on 18/05/2016.
 */
public class MeGridAdapterDelegate implements AdapterDelegate<List<Friendship>> {

    protected LayoutInflater layoutInflater;
    @Inject PaletteGrid paletteGrid;

    public MeGridAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
        return (position == 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new MeGridViewHolder(layoutInflater.inflate(R.layout.item_me_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        MeGridViewHolder vh = (MeGridViewHolder) holder;
        User me = (User) items.get(position);

        vh.txtName.setText(me.getDisplayName());
        vh.avatar.load(me.getProfilePicture());
        vh.txtPoints.setText(me.getScoreStr());
    }

    static class MeGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContent) public ViewGroup layoutContent;
        @BindView(R.id.avatar) public AvatarView avatar;
        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.txtPoints) public TextViewFont txtPoints;

        public MeGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
