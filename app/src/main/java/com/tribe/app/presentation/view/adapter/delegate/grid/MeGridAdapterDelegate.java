package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class MeGridAdapterDelegate extends RxAdapterDelegate<List<Recipient>> {

    protected LayoutInflater layoutInflater;
    private Context context;

    @Inject
    PaletteGrid paletteGrid;

    @Inject
    Picasso picasso;

    // RESOURCES
    private int avatarSize;

    // OBSERVABLES
    protected final PublishSubject<View> clickOpenPoints = PublishSubject.create();
    protected final PublishSubject<View> clickOpenSettings = PublishSubject.create();

    public MeGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
    }

    @Override
    public boolean isForViewType(@NonNull List<Recipient> items, int position) {
        return (position == 0);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        MeGridViewHolder vh = new MeGridViewHolder(layoutInflater.inflate(R.layout.item_me_grid, parent, false));

        vh.layoutPoints.setOnClickListener(v -> clickOpenPoints.onNext(vh.layoutPoints));
        vh.layoutSettings.setOnClickListener(v -> clickOpenSettings.onNext(vh.layoutSettings));

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Recipient> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        MeGridViewHolder vh = (MeGridViewHolder) holder;
        User me = ((Friendship) items.get(position)).getFriend();

        vh.txtName.setText(me.getDisplayName());
        vh.txtPoints.setText(me.getScoreStr());
        vh.imgLevel.setImageResource(ScoreUtils.getLevelForScore(me.getScore()).getDrawableId());

        if (!StringUtils.isEmpty(me.getProfilePicture())) {
            picasso.load(me.getProfilePicture())
                    .fit()
                    .centerCrop()
                    .transform(new RoundedCornersTransformation(avatarSize >> 1, 0, RoundedCornersTransformation.CornerType.ALL))
                    .into(vh.avatar);
        }
    }

    public PublishSubject<View> clickOpenPoints() {
        return clickOpenPoints;
    }

    public PublishSubject<View> clickOpenSettings() {
        return clickOpenSettings;
    }

    static class MeGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.layoutContent) public ViewGroup layoutContent;
        @BindView(R.id.imgAvatar) public ImageView avatar;
        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.txtPoints) public TextViewFont txtPoints;
        @BindView(R.id.imgLevel) public ImageView imgLevel;
        @BindView(R.id.layoutPoints) public ViewGroup layoutPoints;
        @BindView(R.id.btnSettings) public RelativeLayout layoutSettings;

        public MeGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
